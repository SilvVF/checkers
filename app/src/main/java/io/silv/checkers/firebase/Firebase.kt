package io.silv.checkers.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import io.silv.checkers.Board
import io.silv.checkers.Room
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDateTime
import java.time.ZoneOffset

object Fb {

    const val roomsKey = "rooms"
    const val boardKey = "boards"
}

fun DatabaseReference.createRoomFlow(name: String, color: Int, userId: String) = callbackFlow {
    val key = this@createRoomFlow.child(Fb.roomsKey).push().key ?: kotlin.run {
        close(IllegalStateException("unable to create room"))
        return@callbackFlow
    }
    val room = Room(
        id = key,
        name = name,
        users = mapOf(userId to color),
        createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    )
    val roomValues = room.toMap()
    val boardValues = Board(key)

    val childUpdates = mapOf(
        "/${Fb.roomsKey}/$key" to roomValues,
        "/${Fb.boardKey}/$key" to boardValues
    )
    this@createRoomFlow.updateChildren(childUpdates)
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

fun DatabaseReference.roomStateFlow(id: String) = callbackFlow {
    val roomListener = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot) {
           dataSnapshot.getValue<Room>()?.let {
               trySend(it)
           }
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Post failed, log a message
            Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            close(databaseError.toException())
        }
    }
    val roomIdNode = this@roomStateFlow.child(Fb.roomsKey).child(id)
    roomIdNode.addValueEventListener(roomListener)
    awaitClose { roomIdNode.removeEventListener(roomListener) }
}