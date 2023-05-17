package io.silv.checkers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.ui.Empty
import io.silv.checkers.ui.dragdrop.generateInitialBoard
import io.silv.checkers.ui.theme.DragDropTestTheme
import io.silv.checkers.validation.validatePlacement


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

                BackHandler {
                    // stop dragging from closing the app
                }

                CheckerBoard(
                    board = board,
                    onDropAction = { fromCord, toCord, piece ->
                        val result = validatePlacement(board, fromCord, toCord)
                        if (!result.valid) {
                            return@CheckerBoard
                        }
                        board = List(8) { i ->
                            List(8) { j ->
                                when (i to j) {
                                    in result.captured -> Empty
                                    fromCord -> Empty
                                    toCord -> piece
                                    else -> board[i][j]
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}





