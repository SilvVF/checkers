package io.silv.checkers.usecase

import android.util.Log
import io.silv.checkers.Blue
import io.silv.checkers.Cord
import io.silv.checkers.Empty
import io.silv.checkers.Piece
import io.silv.checkers.Red
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull


class AiOpponent {

    private val KEY = "AiOpponent"

    suspend fun makeMove(board: List<List<Piece>>, depth: Int = 2): List<List<Piece>> = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(5000) {
             minimax(board, depth, true)
        }
        Log.d(KEY, "evaluation ${result?.first}")
        if (result?.second == board || result?.second == null) {
            Log.d(KEY, "used Random board")
            getAllBoardsDeferred(board, Red())
                .filter { it != board }
                .also { Log.d(KEY, "board count ${it.size}") }
                .ifEmpty { listOf(board) }
                .random()
                .crownPieces()
        } else{
            result.second.crownPieces()
        }
    }

    private suspend fun minimax(
        board: List<List<Piece>>,
        depth: Int,
        maxPlayer: Boolean,
    ): Pair<Double, List<List<Piece>>> {
        if (depth == 0) {
            return (evaluate(board) to board)
        }
        if (maxPlayer) {
            var value = Double.MIN_VALUE
            var bestBoard: List<List<Piece>> = board
            for (b in getAllBoardsDeferred(board, Red())) {
                val v = minimax(b, depth - 1, false).first
                if (v > value) {
                    value = v
                    bestBoard = b
                }
            }
            return value to bestBoard
        } else {
            var value = Double.MAX_VALUE
            var bestBoard: List<List<Piece>> = board
            for (b in getAllBoardsDeferred(board, Blue())) {
                val v = minimax(b, depth - 1, true).first
                if (v < value) {
                    value = v
                    bestBoard = b
                }
            }
            return value to bestBoard
        }
    }

    private fun evaluate(board: List<List<Piece>>): Double {
        val blueCount = board.sumOf { row ->
            row.count {
                it.value == Blue().value
            }
        }
        val redCount = board.sumOf { row ->
            row.count {
                it.value == Red().value
            }
        }
        val blueKings = board.sumOf {row ->
            row.count { it.crowned && it.value == Blue().value }
        }
        val redKings = board.sumOf {row ->
            row.count { it.crowned && it.value == Red().value }
        }
        return  redCount - blueCount + (redKings * 0.5 - blueKings * 0.5)
    }

    private fun getAllPieces(
        board: List<List<Piece>>,
        piece: Piece
    ) = board.flatMap { row -> row.filter{ it.value == piece.value }  }


    private fun simulateMove(from: Cord, to: Cord, jumped: Cord?, board: List<List<Piece>>): List<List<Piece>> {
        return List(8) { i ->
            List(8) { j ->
                when(i to j) {
                    from  -> Empty
                    to -> board[from.first][from.second]
                    jumped -> Empty
                    else -> board[i][j]
                }
            }
        }
    }

    private suspend fun getAllBoardsDeferred(board: List<List<Piece>>, piece: Piece) =
        withContext(Dispatchers.IO) {
            getAllPieces(board, piece).map { piece ->
                async {
                    validMoves(board, piece).map { result ->
                        simulateMove(
                            from = result.from,
                            to = result.to,
                            jumped = result.jumped,
                            board = board
                        )
                    }
                }
            }
                .awaitAll()
                .flatten()
    }

    private data class ValidMove(
        val to: Cord,
        val from: Cord,
        val jumped: Cord?
    )

    private fun validMoves(board: List<List<Piece>>, piece: Piece): List<ValidMove>  {
        val nonCrownedDirection = if (piece.value == Red().value) {
            listOf(XYDirection.DownRight ,XYDirection.DownLeft)
        } else {
            listOf(XYDirection.UpLeft, XYDirection.UpRight)
        }
        val directions = listOf(XYDirection.UpLeft, XYDirection.UpRight, XYDirection.DownRight ,XYDirection.DownLeft)

        val validJumps = buildList {
            board.forEachPieceIndexed(
                filterPiece = piece
            ) { i, j, p ->
                val from = i to j
                for (direction in if(piece.crowned) directions else nonCrownedDirection) {
                    val to = getDiagonalCord(from, direction, 2) ?: return@forEachPieceIndexed
                    if (validatePlacement(board, from, to).valid) {
                        add(
                            ValidMove(
                                from = from,
                                to = to,
                                jumped = getDiagonalCord(from, direction) ?: return@forEachPieceIndexed
                            )
                        )
                    }
                }
            }
        }

        val validSingleSpaceMoves = buildList {
            board.forEachPieceIndexed(filterPiece = piece) { i, j, p ->
                if (piece.value != p.value) {
                    return@forEachPieceIndexed
                }
                val from = i to j
                for (direction in if(piece.crowned) directions else nonCrownedDirection) {
                    val to = getDiagonalCord(from, direction) ?: return@forEachPieceIndexed
                    val result = validatePlacement(board, from, to)
                    if (result.valid) {
                        add(
                            ValidMove(
                                from = from,
                                to = to,
                                jumped = null
                            )
                        )
                    }
                }
            }
        }
        return validSingleSpaceMoves + validJumps
    }
}

fun List<List<Piece>>.forEachPieceIndexed(filterPiece: Piece? = null, action: (i: Int, j: Int, p: Piece) -> Unit) {
    this.forEachIndexed { i, pieces ->
        pieces.forEachIndexed { j, piece ->
            if (filterPiece == null || piece.value == filterPiece.value) {
                action(i, j, piece)
            }
        }
    }
}