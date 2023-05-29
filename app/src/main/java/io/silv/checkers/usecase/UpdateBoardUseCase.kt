package io.silv.checkers.usecase

import com.google.firebase.database.DatabaseReference
import io.silv.checkers.Board
import io.silv.checkers.Cord
import io.silv.checkers.JsonPieceList
import io.silv.checkers.Move
import io.silv.checkers.firebase.updateBoardCallbackFlow
import io.silv.checkers.toJsonPiece
import io.silv.checkers.toPieceList
import kotlinx.coroutines.flow.first

class UpdateBoardUseCase(
    private val db: DatabaseReference
) {

    suspend operator fun invoke(
        board: Board,
        from: Cord,
        to: Cord,
        roomId: String,
    ): Result<Board> {
        val (valid, removed, newData) = validatePlacement(board.data.toPieceList(), from, to)
        if (!valid) { return Result.failure(Exception("Invalid Move")) }
        val newBoard = board.copy(
            turn = when {
                removed != null && moreJumpsPossible(newData, to) -> board.turn
                board.turn == 1 -> 2
                else -> 1
            },
            data = JsonPieceList(
                list = newData.map {
                    it.map { p -> p.toJsonPiece() }
                }
            ),
            moves = board.moves + Move(
                to = listOf(to.first, to.second),
                from = listOf(from.first, from.second)
            )
        )
        return runCatching {
            db.updateBoardCallbackFlow(newBoard, roomId)
                .first()
            newBoard
        }
    }
}