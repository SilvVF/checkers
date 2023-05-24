package io.silv.checkers.usecase

import android.util.Log
import io.silv.checkers.Blue
import io.silv.checkers.Cord
import io.silv.checkers.Empty
import io.silv.checkers.Piece
import io.silv.checkers.Red
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext


suspend fun aiMove(board: List<List<Piece>>) = minimax(board, 3, true)


fun minimax(
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
        for (b in getAllBoards(board, Red())) {
            val (v) = minimax(b, depth - 1, false)
            if (v >= value) {
                value = v
                bestBoard = b
            }
        }
       return value to bestBoard
    } else {
        var value = Double.MAX_VALUE
        var bestBoard: List<List<Piece>> = board
        for (b in getAllBoards(board, Blue())) {
            val (v) = minimax(b, depth - 1, true)
            if (v <= value) {
                value = v
                bestBoard = b
            }
        }
        return value to bestBoard
    }
}


fun evaluate(board: List<List<Piece>>): Double {
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
    return  blueCount - redCount + (blueKings * 0.5 - redKings * 0.5)
}

fun getAllPieces(
    board: List<List<Piece>>,
    piece: Piece
) = board.flatMap { row -> row.filter{ it.value == piece.value }  }


fun simulateMove(from: Cord, toC: Cord, skip: Cord?, board: List<List<Piece>>): List<List<Piece>> {
    return List(8) { i ->
        List(8) { j ->
            when(i to j) {
                from  -> Empty
                toC -> board[from.first][from.second]
                skip -> Empty
                else -> board[i][j]
            }
        }
    }
}

fun getAllBoards(board: List<List<Piece>>, piece: Piece):  List<List<List<Piece>>> {
    return getAllPieces(board, piece).flatMap { piece ->
        validMoves(board, piece).map { (move, skip) ->
            val (from, to) = move
            simulateMove(from, to, skip, board)
        }
    }
}


fun validMoves(board: List<List<Piece>>, piece: Piece): List<Pair<Pair<Cord, Cord>, Cord?>> {
    val blueDirections = listOf(XYDirection.UpLeft, XYDirection.UpRight)
    val redDirections = listOf(XYDirection.DownRight ,XYDirection.DownLeft)
    val directions = blueDirections + redDirections

    fun getValidJumps(
        blue: Boolean
    ): List<Pair<Pair<Cord, Cord>, Cord?>> =
        buildList {
            board.forEachIndexed { i, row ->
                row.forEachIndexed { j, piece ->
                    if (blue && piece is Blue || !blue && piece is Red) {
                        directions.forEach {
                            if (validateJump(board, i to j,board[i][j], it)){
                                val skip = getDiagonalCord(i to j, it)!!
                                add(
                                    i to j to getDiagonalCord(skip, it)!! to skip
                                )
                            }
                        }
                    }
                }
            }
        }

    fun getMovesNoJump(
        blue: Boolean
    ): List<Pair<Pair<Cord, Cord>, Cord?>> =
        buildList {
            board.forEachIndexed { i, row ->
                row.forEachIndexed { j, piece ->
                    if (blue && piece is Blue || !blue && piece is Red) {
                        directions.mapNotNull { getDiagonalCord(i to j, it) }.forEach {
                            runCatching {
                                if(validatePlacement(board, i to j, it).valid) {
                                    add(
                                        (i to j) to it to null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    return getMovesNoJump(
        piece.value == Blue().value
    ) + getValidJumps(
        piece.value == Blue().value
    )
}