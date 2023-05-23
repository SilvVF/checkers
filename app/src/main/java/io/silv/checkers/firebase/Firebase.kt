package io.silv.checkers.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import io.silv.checkers.Blue
import io.silv.checkers.Board
import io.silv.checkers.Cord
import io.silv.checkers.Empty
import io.silv.checkers.Red
import io.silv.checkers.Room
import io.silv.checkers.User
import io.silv.checkers.toJsonPiece
import io.silv.checkers.usecase.validatePlacement
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDateTime
import java.time.ZoneOffset

object Fb {

    const val roomsKey = "rooms"
    const val boardKey = "boards"
    const val usersKey = "users"
}

fun DatabaseReference.createUserFlow(userId: String, roomId: String? = null) = callbackFlow {
    val db = this@createUserFlow
    val key = db.child(Fb.usersKey).child(userId)
    val user = User(
        id = userId,
        joinedRoomId = roomId ?: ""
    )
    val updates = mapOf("/${Fb.usersKey}/$key" to user.toMap())
    db.updateChildren(updates)
        .addOnSuccessListener {
            trySend(true)
        }
        .addOnFailureListener {
            close(it)
        }
    awaitClose()
}

fun DatabaseReference.createRoomFlow(name: String, color: Int, userId: String, time: Int) = callbackFlow {
    val db = this@createRoomFlow
    val key = db.child(Fb.roomsKey).push().key ?: kotlin.run {
        close(IllegalStateException("unable to create room"))
        return@callbackFlow
    }
    val room = Room(
        id = key,
        name = name,
        usersToColorChoice = mapOf(userId to color),
        moveTimeSeconds = time ,
        createdAtEpochSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    )
    val roomValues = room.toMap()
    val boardValues = Board(key).toMap()

    val childUpdates = mapOf(
        "/${Fb.roomsKey}/$key" to roomValues,
        "/${Fb.boardKey}/$key" to boardValues
    )
    db.updateChildren(childUpdates)
        .addOnSuccessListener {
            trySend(key)
        }
        .addOnFailureListener {
            close(it)
        }
    awaitClose()
}

fun DatabaseReference.roomsFlow() = callbackFlow {
    val roomsListener = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val rooms = dataSnapshot.children.mapNotNull {
                runCatching { it.getValue<Room>() }
                    .onFailure { it.printStackTrace() }
                    .getOrNull()
            }
            trySend(rooms)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Post failed, log a message
            Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            close(databaseError.toException())
        }
    }
    val roomsNode = this@roomsFlow.child(Fb.roomsKey)
    roomsNode.addValueEventListener(roomsListener)
    awaitClose { roomsNode.removeEventListener(roomsListener) }
}

fun DatabaseReference.boardStateFlow(roomId: String) = callbackFlow {
    Log.d("FB", "board ID$roomId")
    val boardListener =  object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            snapshot.getValue<Board>()?.let {
                Log.d("FB", "boardReceivied$it")
                trySend(it)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("FB", "loadPost:onCancelled", error.toException())
            close(error.toException())
        }
    }
    val boardNode = this@boardStateFlow.child(Fb.boardKey).child(roomId)
    boardNode.addValueEventListener(boardListener)
    awaitClose { boardNode.removeEventListener(boardListener) }
}

fun DatabaseReference.roomStateFlow(id: String) = callbackFlow {
    val roomListener = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot) {
           dataSnapshot.getValue<Room>()?.let {
               Log.d("FB", "room received $it")
               trySend(it)
           }
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Post failed, log a message
            Log.w("FB", "loadPost:onCancelled", databaseError.toException())
            close(databaseError.toException())
        }
    }
    val roomIdNode = this@roomStateFlow.child(Fb.roomsKey).child(id)
    roomIdNode.addValueEventListener(roomListener)
    awaitClose { roomIdNode.removeEventListener(roomListener) }
}
fun DatabaseReference.updateBoardNoMove(board: Board, roomId: String) = callbackFlow {
    val db = this@updateBoardNoMove
    val boardNode = db.child(Fb.boardKey).child(roomId)
    boardNode.updateChildren(
        board.copy(
            turn = if (board.turn == 1) 2 else 1,
        ).toMap()
    )
        .addOnSuccessListener {
            trySend(true)
        }
        .addOnFailureListener {
            trySend(false)
        }
    awaitClose()
}

fun DatabaseReference.updateBoardCallbackFlow(board: Board, from: Cord, to: Cord, roomId: String) = callbackFlow {
    val db = this@updateBoardCallbackFlow
    val boardNode = db.child(Fb.boardKey).child(roomId)
    val (valid, newBoard) = validatePlacement(board.toDomain(), from, to)
    if (valid) {
        boardNode.updateChildren(
            board.copy(
                turn = if (board.turn == 1) 2 else 1,
                data = newBoard.map { list -> list.map { it.toJsonPiece() } },
                roomId = board.roomId,
                moves = board.moves + listOf(from to to)
            )
                .toMap()
        )
            .addOnSuccessListener {
                trySend(true)
            }
            .addOnFailureListener {
                trySend(false)
            }
    } else {
        trySend(false)
    }
    awaitClose()
}



fun Board.toDomain() = this.data.map {list ->
    list.map { jsonPiece ->
        when (jsonPiece.value) {
            Red().value -> Red(jsonPiece.crowned)
            Blue().value -> Blue(jsonPiece.crowned)
            else -> Empty
        }
    }
}