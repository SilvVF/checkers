package io.silv.checkers.validation

import io.silv.checkers.ui.Blue
import io.silv.checkers.ui.Empty
import io.silv.checkers.ui.Piece
import io.silv.checkers.ui.Red
import io.silv.checkers.ui.dragdrop.Cord
import kotlin.math.abs

sealed interface Direction

sealed interface XYDirection: Direction {
    object UpLeft: XYDirection
    object DownLeft: XYDirection
    object UpRight: XYDirection
    object DownRight: XYDirection
    object None: XYDirection
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

fun getDiagonalCord(from: Cord, xyDirection: XYDirection): Cord? {
    return runCatching {
        when (xyDirection) {
            XYDirection.DownLeft -> from.first + 1 to from.second - 1
            XYDirection.DownRight -> from.first + 1 to from.second + 1
            XYDirection.UpLeft -> from.first - 1 to from.second - 1
            XYDirection.UpRight -> from.first - 1 to from.second + 1
            XYDirection.None -> null
        }
    }.getOrNull()
}

fun List<List<Piece>>.getDiagonal(from: Cord, xyDirection: XYDirection): Piece? {
    return when (xyDirection) {
        XYDirection.DownLeft -> this
            .getOrNull(from.first + 1)
            ?.getOrNull(from.second - 1)
        XYDirection.DownRight -> this
            .getOrNull(from.first + 1)
            ?.getOrNull(from.second + 1)
        XYDirection.UpLeft -> this
            .getOrNull(from.first - 1)
            ?.getOrNull(from.second - 1)
        XYDirection.UpRight -> this
            .getOrNull(from.first - 1)
            ?.getOrNull(from.second + 1)
        XYDirection.None -> null
    }
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

data class MoveResult(
    val valid: Boolean,
    val captured: List<Cord>
)

/**
 * When this function is called To cord is assumed to be 2 spaces away relative to the From Cord
 * and in the direction passed as XYDirection.
 */
fun validateJump(
    board: List<List<Piece>>,
    from: Cord,
    piece: Piece,
    direction: XYDirection
): Boolean {
    if (!piece.crowned) {
        return when (piece) {
            is Red -> {
                when (direction) {
                    XYDirection.DownRight -> board.getDiagonal(from, XYDirection.DownRight) is Blue
                    XYDirection.DownLeft -> board.getDiagonal(from, XYDirection.DownLeft) is Blue
                    else -> false
                }
            }
            is Blue -> {
                when (direction) {
                    XYDirection.UpLeft ->  board.getDiagonal(from, XYDirection.UpLeft) is Red
                    XYDirection.UpRight ->  board.getDiagonal(from, XYDirection.UpRight) is Red
                    else -> false
                }
            }
            else -> false
        }
    } else { // crowned
        return when (piece) {
            is Red -> {
                when (direction) {
                    XYDirection.DownRight -> board.getDiagonal(from, XYDirection.DownRight) is Blue
                    XYDirection.DownLeft -> board.getDiagonal(from, XYDirection.DownLeft) is Blue
                    XYDirection.UpLeft ->  board.getDiagonal(from, XYDirection.UpLeft) is Blue
                    XYDirection.UpRight ->  board.getDiagonal(from, XYDirection.UpRight) is Blue
                    else -> false
                }
            }
            is Blue -> {
                when (direction) {
                    XYDirection.DownRight -> board.getDiagonal(from, XYDirection.DownRight) is Red
                    XYDirection.DownLeft -> board.getDiagonal(from, XYDirection.DownLeft) is Red
                    XYDirection.UpLeft ->  board.getDiagonal(from, XYDirection.UpLeft) is Red
                    XYDirection.UpRight ->  board.getDiagonal(from, XYDirection.UpRight) is Red
                    else -> false
                }
            }
            else -> false
        }
    }
}


fun validatePlacement(board: List<List<Piece>>, from: Cord, to: Cord): MoveResult {
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
        (piece is Red && yDirection != YDirection.Down) && !piece.crowned ||
        (piece is Blue && yDirection != YDirection.Up) && !piece.crowned
    ) {
        return bad
    }
    // moving to non empty square
    if (board[to.first][to.second] !is Empty) {
        return bad
    }
    if (xyDirection == XYDirection.None) {
        return bad
    }

    // moving diagonal 1 in correct direction and to is empty
    getDiagonalCord(from, xyDirection)?.let { cord ->
        return when {
            distX == 1 && distY == 1 -> {
                MoveResult(true, emptyList())
            }
            distX == 2 && distY == 2 -> {
                if (validateJump(board, from, piece, xyDirection)) {
                    MoveResult(true, listOf(cord))
                } else {
                    bad
                }
            }
            else -> { // moved distance greater than one hop which is invalid
                bad
            }
        }
    }
    return bad
}