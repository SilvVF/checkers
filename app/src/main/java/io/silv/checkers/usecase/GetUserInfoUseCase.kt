package io.silv.checkers.usecase

import com.google.firebase.database.DatabaseReference
import io.silv.checkers.User
import io.silv.checkers.firebase.getUserCallbackFlow
import kotlinx.coroutines.flow.first

class GetUserInfoUseCase(
    private val db: DatabaseReference
) {

    suspend operator fun invoke(userId: String): Result<User> {
        return runCatching {
            db.getUserCallbackFlow(userId)
                .first()
        }
    }
}