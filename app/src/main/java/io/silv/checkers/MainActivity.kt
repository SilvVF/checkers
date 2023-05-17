package io.silv.checkers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import io.silv.checkers.ui.Blue
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.ui.Empty
import io.silv.checkers.ui.Piece
import io.silv.checkers.ui.Red
import io.silv.checkers.ui.dragdrop.generateInitialBoard
import io.silv.checkers.ui.theme.DragDropTestTheme
import io.silv.checkers.validation.validatePlacement

fun Turn.correctPieceForTurn(piece: Piece): Boolean {
    return when (this) {
        Turn.Red -> piece is Red
        Turn.Blue -> piece is Blue
    }
}

enum class Turn {
    Red, Blue
}

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DragDropTestTheme {

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
        }
    }
}





