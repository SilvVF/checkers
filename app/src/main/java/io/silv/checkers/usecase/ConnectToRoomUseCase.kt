package io.silv.checkers.usecase

import com.google.firebase.database.DatabaseReference
import io.silv.checkers.User
import io.silv.checkers.firebase.deleteBoardCallbackFlow
import io.silv.checkers.firebase.deleteRoomCallbackFlow
import io.silv.checkers.firebase.getUserCallbackFlow
import io.silv.checkers.firebase.joinRoom
import io.silv.checkers.firebase.updateUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class ConnectToRoomUseCase(
    private val db: DatabaseReference
) {

    private suspend fun deleteRoomIfJoined(userId: String, roomId: String) = CoroutineScope(Dispatchers.IO).launch {
        withTimeout(5000) {
            with(db.getUserCallbackFlow(userId).first()) {
                if (this.joinedRoomId != roomId) {
                    db.deleteRoomCallbackFlow(this.joinedRoomId).first()
                    db.deleteBoardCallbackFlow(this.joinedRoomId).first()
                }
            }
        }
    }
    private suspend fun updateUser(user: User) = CoroutineScope(Dispatchers.IO).launch {
        withTimeout(5000) {
            db.updateUser(user).first()
        }
    }

    suspend operator fun invoke(roomId: String, userId: String): Result<String> = withContext(Dispatchers.IO) {
        deleteRoomIfJoined(userId, roomId)
        runCatching {
            db.joinRoom(roomId, userId)
                .onEach {
                    updateUser(User(userId, roomId))
                }
                .first()
            roomId
        }
    }
}