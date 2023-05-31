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
import kotlinx.coroutines.withTimeout

class CreateRoomUseCase(
    private val db: DatabaseReference
) {


    private suspend fun updateUser(user: User) = CoroutineScope(Dispatchers.IO).launch {
        withTimeout(5000) {
            db.updateUser(user).first()
        }
    }

    suspend operator fun invoke(name: String, color: Int, userId: String, moveTime: Int): Result<String> {
        return runCatching {
            db.createRoomFlow(
                name, color, userId, moveTime
            )
                .onEach {
                    updateUser(
                        User(userId, it)
                    )
                }
                .first()
        }
    }
}