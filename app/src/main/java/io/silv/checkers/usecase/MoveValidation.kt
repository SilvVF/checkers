package io.silv.checkers.usecase

import io.silv.checkers.Blue
import io.silv.checkers.Cord
import io.silv.checkers.Empty
import io.silv.checkers.Piece
import io.silv.checkers.Red
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

private fun getXyDirection(x: XDirection, y: YDirection): XYDirection {
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

fun getDiagonalCord(from: Cord, xyDirection: XYDirection, dist: Int = 1): Cord? {
    return when (xyDirection) {
            XYDirection.DownLeft -> from.first + dist to from.second - dist
            XYDirection.DownRight -> from.first + dist to from.second + dist
            XYDirection.UpLeft -> from.first - dist to from.second - dist
            XYDirection.UpRight -> from.first - dist to from.second + dist
            XYDirection.None -> null
    }
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

fun List<List<Piece>>.crownPieces() = this.mapIndexed { i, row ->
    row.mapIndexed { j , p ->
        when {
            i == 0 && p is Blue -> Blue(true)
            i == this.lastIndex && p is Red -> Red(true)
            else -> p
        }
    }
}

private fun getYDirection(difY: Int): YDirection {
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

private fun getXDirection(difX: Int): XDirection {
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
    val removed: Cord?,
    val data: List<List<Piece>>
)

fun moreJumpsPossible(board: List<List<Piece>>, lastMoveEnd: Cord): Boolean {
    val piece = board.getOrNull(lastMoveEnd.first)?.getOrNull(lastMoveEnd.second) ?: return false
    for (direction in listOf(XYDirection.UpLeft, XYDirection.UpRight, XYDirection.DownLeft, XYDirection.DownRight)) {
        val jumpedCord = getDiagonalCord(lastMoveEnd, direction) ?: continue
        val endOfJumpCord = getDiagonalCord(jumpedCord, direction) ?: continue
        val toSpaceIsEmpty = board.getOrNull(endOfJumpCord.first)?.getOrNull(endOfJumpCord.second) is Empty
        if (toSpaceIsEmpty && validateJump(board, lastMoveEnd, piece, direction)) {
            return true
        }
    }
    return false
}

/**
 * When this function is called To cord is assumed to be 2 spaces away relative to the From Cord
 * and in the direction passed as XYDirection.
 */
private fun validateJump(
    board: List<List<Piece>>,
    from: Cord,
    piece: Piece,
    direction: XYDirection
): Boolean {
    if (!piece.crowned) {
        return when (piece) { // piece is not crowned and can only move in specific XYDirection's
            is Red -> {
                when (direction) {
                    XYDirection.DownLeft, XYDirection.DownRight -> board.getDiagonal(from, direction) is Blue
                    else -> false
                }
            }
            is Blue -> {
                when (direction) {
                    XYDirection.UpLeft, XYDirection.UpRight ->  board.getDiagonal(from, direction) is Red
                    else -> false
                }
            }
            else -> false
        }
    } else { // crowned
        // can move any XYDirection except None getDiagonal returns null if direction is XYDirection.None
        return when (piece) {
            is Red -> {
                board.getDiagonal(from, direction) is Blue
            }
            is Blue -> {
               board.getDiagonal(from, direction) is Red
            }
            else -> false
        }
    }
}


fun validatePlacement(board: List<List<Piece>>, from: Cord, to: Cord): MoveResult {
    val bad = MoveResult(valid = false,null,  board)
    if (to.first > board.lastIndex || to.first < 0 || to.second > board[0].lastIndex || to.second < 0) {
        return bad
    }
    val piece = board[from.first][from.second]
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
    getDiagonalCord(from, xyDirection).let { cord ->

        return when {
            distX == 1 && distY == 1 -> {
                MoveResult(
                    valid = true,
                    removed = null,
                    data = List(8) { i ->
                        List(8) { j ->
                            when(i to j)  {
                                to -> piece
                                from -> Empty
                                else -> board[i][j]
                            }
                        }
                    }.crownPieces()
                )
            }
            distX == 2 && distY == 2 -> {
                if (validateJump(board, from, piece, xyDirection)) {
                    MoveResult(
                        valid = true,
                        removed = getDiagonalCord(from, xyDirection),
                        data = List(8) {i ->
                            List(8) { j ->
                                when(i to j)  {
                                    to -> piece
                                    from -> Empty
                                    cord -> Empty
                                    else -> board[i][j]
                                }
                            }
                        }.crownPieces()
                    )
                } else {
                    bad
                }
            }
            else -> { // moved distance greater than one hop which is invalid
                    // double jumps happen one move at a time
                bad
            }
        }
    }
}


fun checkPieceForLoss(board: List<List<Piece>>, piece: Piece): Boolean {

    val pieceCount = board.sumOf { row -> row.count { p -> p.value == piece.value } }

    val blueDirections = listOf(XYDirection.UpLeft, XYDirection.UpRight)
    val redDirections = listOf(XYDirection.DownRight ,XYDirection.DownLeft)
    val nonCrownedDirections = if (Red().value == piece.value) redDirections else blueDirections
    val directions = blueDirections + redDirections

    val anyValidJumps = buildList {
        board.forEachPieceIndexed(
            filterPiece = piece
        ) { i, j, p ->
            val from = i to j
            for (direction in if (p.crowned) directions else nonCrownedDirections) {
                val jumpCord = getDiagonalCord(from, direction, 2) ?: continue
                add(validatePlacement(board, from, jumpCord).valid)
            }
        }
    }
        .any { valid -> valid }

    val anySingleMoves = buildList {
        board.forEachPieceIndexed(filterPiece = piece) { i, j, p ->
            val from = i to j
            for (direction in if (p.crowned) directions else nonCrownedDirections) {
                val to = getDiagonalCord(from, direction) ?: continue
                add(validatePlacement(board, from, to).valid)
            }
        }
    }.any { valid -> valid  }
    return (!anySingleMoves && !anyValidJumps) || pieceCount <= 0
}