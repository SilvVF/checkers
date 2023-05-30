package io.silv.checkers.usecase

import com.google.firebase.database.DatabaseReference
import io.silv.checkers.User
import io.silv.checkers.firebase.createRoomFlow
import io.silv.checkers.firebase.deleteRoomCallbackFlow
import io.silv.checkers.firebase.getUserCallbackFlow
import io.silv.checkers.firebase.updateUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CreateRoomUseCase(
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

    suspend operator fun invoke(name: String, color: Int, userId: String, moveTime: Int): Result<String> {
        return runCatching {

            db.createRoomFlow(
                name, color, userId, moveTime
            ).catch {
                it.printStackTrace()
            }
                .onEach {
                    coroutineScope {
                        launch { deleteRoomIfJoined(userId, it) }
                    }
                }
                .first()
        }
    }
}