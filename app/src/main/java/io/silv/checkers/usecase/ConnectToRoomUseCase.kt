package io.silv.checkers.usecase

import com.google.firebase.database.DatabaseReference
import io.silv.checkers.User
import io.silv.checkers.firebase.deleteRoomCallbackFlow
import io.silv.checkers.firebase.getUserCallbackFlow
import io.silv.checkers.firebase.joinRoom
import io.silv.checkers.firebase.updateUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnectToRoomUseCase(
    private val db: DatabaseReference
) {

    private suspend fun deleteRoomIfJoined(userId: String, roomId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            val user = db.getUserCallbackFlow(userId)
                .first()
            db.updateUser(
                User(id = userId, joinedRoomId = roomId)
            )
                .first()
            if (user.joinedRoomId.isNotEmpty() && roomId != user.joinedRoomId) {
                db.deleteRoomCallbackFlow(user.joinedRoomId)
                    .catch {
                        it.printStackTrace()
                    }
                    .first()
            }
    }

    suspend operator fun invoke(roomId: String, userId: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching { deleteRoomIfJoined(userId, roomId) }
        runCatching {
            db.joinRoom(roomId, userId)
                .catch {
                    it.printStackTrace()
                }
                .first()
            roomId
        }
    }
}