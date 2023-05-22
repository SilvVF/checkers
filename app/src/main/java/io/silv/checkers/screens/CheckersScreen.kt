package io.silv.checkers.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.silv.checkers.Blue
import io.silv.checkers.Empty
import io.silv.checkers.Piece
import io.silv.checkers.Red
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.usecase.generateInitialBoard
import io.silv.checkers.usecase.validatePlacement

fun Turn.correctPieceForTurn(piece: Piece): Boolean {
    return when (this) {
        Turn.Red -> piece is Red
        Turn.Blue -> piece is Blue
    }
}

enum class Turn {
    Red, Blue
}


@Composable
fun CheckersScreen() {

    var board by remember {
        mutableStateOf(
            generateInitialBoard()
        )
    }

    var turn by rememberSaveable {
        mutableStateOf(Turn.Red)
    }

    BackHandler {
        // stop dragging from closing the app
    }

    CheckerBoard(
        board = board,
        turn = turn,
        onDropAction = { fromCord, toCord, piece ->
            if (!turn.correctPieceForTurn(piece)) {
                return@CheckerBoard
            }
            val result = validatePlacement(board, fromCord, toCord)
            if (!result.valid) {
                return@CheckerBoard
            }
            board = List(8) { i ->
                List(8) { j ->
                    when (i to j) {
                        in result.captured -> Empty
                        fromCord -> Empty
                        toCord -> when(piece) {
                            is Red -> {
                                if (i == board.lastIndex) { Red(true) } else piece
                            }
                            is Blue -> {
                                if (i == 0) { Blue(true) } else piece
                            }
                            else -> piece
                        }
                        else -> board[i][j]
                    }
                }
            }
            turn = when (turn) {
                Turn.Red -> Turn.Blue
                Turn.Blue -> Turn.Red
            }
        }
    )
}