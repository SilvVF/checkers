package io.silv.checkers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import io.silv.checkers.ui.Blue
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.ui.Empty
import io.silv.checkers.ui.Piece
import io.silv.checkers.ui.Red
import io.silv.checkers.ui.dragdrop.Cord
import io.silv.checkers.ui.dragdrop.generateInitialBoard
import io.silv.checkers.ui.theme.DragDropTestTheme
import java.sql.Driver
import kotlin.math.abs

sealed interface Direction

sealed interface XYDirection: Direction {
    object UpLeft: XYDirection
    object DownLeft: XYDirection
    object UpRight: XYDirection
    object DownRight: XYDirection
    object None: XYDirection
}

fun getXyDirection(x: XDirection, y: YDirection): XYDirection {
    return when (x) {
        XDirection.Left -> {
            when (y) {
                YDirection.Up -> XYDirection.UpLeft
                YDirection.Down -> XYDirection.DownLeft
                else -> XYDirection.None
            }
        }
        XDirection.Right -> {
            when (y) {
                YDirection.Up -> XYDirection.UpRight
                YDirection.Down -> XYDirection.DownRight
                else -> XYDirection.None
            }
        }
        else -> XYDirection.None
    }
}

sealed interface YDirection: Direction {
    object Up: YDirection
    object Down: YDirection
    object None: YDirection
}

sealed interface XDirection: Direction {
    object Left: XDirection
    object Right: XDirection
    object None: XDirection
}

fun getYDirection(difY: Int): YDirection {
    return when {
        difY > 0 -> {
            YDirection.Up
        }
        difY < 0 -> {
            YDirection.Down
        }
        else -> YDirection.None
    }
}

fun getXDirection(difX: Int): XDirection {
    return when  {
        difX < 0 -> { // right
            XDirection.Right
        }
        difX > 0 -> { // left
            XDirection.Left
        }
        else -> XDirection.None
    }
}

fun Int.isNeg() = this < 0


data class MoveResult(
    val valid: Boolean,
    val captured: List<Cord>
)

fun validatePlacement(board: List<List<Piece>>, from: Cord, to: Cord, piece: Piece): MoveResult {
    val piece = board[from.first][from.second]
    val bad = MoveResult(valid = false, emptyList())
    val difX = from.second - to.second
    val difY = from.first - to.first

    val distY = abs(difY)
    val distX = abs(difX)

    val xDirection = getXDirection(difX)
    val yDirection = getYDirection(difY)
    val xyDirection = getXyDirection(xDirection, yDirection)

    // make sure move is not retreating from opponent
    if (
        (piece is Red && yDirection != YDirection.Down) ||
        (piece is Blue && yDirection != YDirection.Up)
    ) {
        return bad
    }
    // moving to non empty square
    if (board[to.first][to.second] !is Empty) {
        return bad
    }
    // moving diagonal 1 in correct direction and to is empty
    if (xyDirection == XYDirection.None) {
        return bad
    }
    if (distY == 1 && distX == 1) {
        return MoveResult(true, emptyList())
    }


    return MoveResult(true, emptyList())
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

                BackHandler {
                    // stop dragging from closing the app
                }

                CheckerBoard(
                    board = board,
                    onDropAction = { fromCord, toCord, piece ->
                        val result = validatePlacement(board, fromCord, toCord, piece)
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





