package io.silv.checkers.usecase

import com.google.firebase.database.DatabaseReference
import io.silv.checkers.firebase.createRoomFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

class CreateRoomUseCase(
    private val db: DatabaseReference
) {

    suspend operator fun invoke(name: String, color: Int, userId: String, moveTime: Int): Result<String> {
        return runCatching {
            db.createRoomFlow(
                name, color, userId, moveTime
            ).catch {
                it.printStackTrace()
            }
                .first()
        }
    }
}