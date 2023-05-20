package io.silv.checkers.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import io.silv.checkers.JsonPieceList
import io.silv.checkers.Room
import io.silv.checkers.toPiece
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json

object Fb {

    lateinit var auth: FirebaseAuth

    lateinit var database: DatabaseReference

    const val roomsKey = "rooms"
    const val usersKey = "users"
    const val userIdKey = "userId"
    const val boardKey = "board"
    const val turnKey = "turn"
    const val nameKey = "name"
}


fun DatabaseReference.roomStateFlow(id: String) = callbackFlow {
    val listener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            trySend(
                Room(
                    id = id,
                    name = dataSnapshot.child(Fb.nameKey).getValue<String>() ?: return,
                    turn = dataSnapshot.child(Fb.turnKey).getValue<Int>() ?: return,
                    users = dataSnapshot.child(Fb.usersKey).children.mapNotNull { child ->
                        child.key
                    },
                    board = Json.decodeFromString(
                        JsonPieceList.serializer(),
                        dataSnapshot.child(Fb.boardKey).getValue<String>() ?: return
                    ).list.map { jsonList ->
                        jsonList.map { jsonPiece ->
                            jsonPiece.toPiece()
                        }
                    }
                )
            )
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Post failed, log a message
            Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            channel.close()
        }
    }
    this@roomStateFlow.child(Fb.roomsKey).child(id).addValueEventListener(listener)
    awaitClose { this@roomStateFlow.removeEventListener(listener) }
}