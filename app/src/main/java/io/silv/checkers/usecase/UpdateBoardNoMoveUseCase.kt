package io.silv.checkers.usecase

import com.google.firebase.database.DatabaseReference
import io.silv.checkers.Board
import io.silv.checkers.firebase.updateBoardCallbackFlow
import kotlinx.coroutines.flow.first


class UpdateBoardNoMoveUseCase(
    private val db: DatabaseReference
) {

    suspend operator fun invoke(board: Board, roomId: String): Result<Boolean> {
        return runCatching {
            db.updateBoardCallbackFlow(
                board.copy(
                    turn = if (board.turn == 1) 2 else 1
                ),
                roomId
            )
                .first()
        }
    }
}