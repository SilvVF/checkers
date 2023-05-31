package io.silv.checkers.usecase

import com.google.firebase.database.DatabaseReference
import io.silv.checkers.firebase.deleteBoardCallbackFlow
import io.silv.checkers.firebase.deleteRoomCallbackFlow
import kotlinx.coroutines.flow.first

class DeleteRoomUseCase(
    private val db: DatabaseReference
) {

    suspend operator fun invoke(roomId: String): Result<Boolean> {
       return runCatching {
           db.deleteBoardCallbackFlow(roomId).first()
           db.deleteRoomCallbackFlow(roomId).first()
       }
    }
}