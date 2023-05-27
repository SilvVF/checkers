package io.silv.checkers.usecase

import com.google.firebase.database.DatabaseReference
import io.silv.checkers.User
import io.silv.checkers.firebase.deleteRoomCallbackFlow
import io.silv.checkers.firebase.getUserCallbackFlow
import io.silv.checkers.firebase.joinRoom
import io.silv.checkers.firebase.updateUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConnectToRoomUseCase(
    private val db: DatabaseReference
) {

    private suspend fun deleteRoomIfJoined(roomId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            if (roomId.isNotEmpty()) {
                db.deleteRoomCallbackFlow(roomId)
                    .catch {
                        it.printStackTrace()
                    }
                    .first()
            }
    }

    suspend operator fun invoke(roomId: String, userId: String): Result<String> {
        return runCatching {
            val user = db.getUserCallbackFlow(userId)
                .first()
            deleteRoomIfJoined(user.joinedRoomId)
            db.updateUser(
                User(id = userId, joinedRoomId = roomId)
            )
            db.joinRoom(roomId, userId)
            roomId
        }
    }
}