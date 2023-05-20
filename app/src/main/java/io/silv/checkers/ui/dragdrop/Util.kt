package io.silv.checkers.ui.dragdrop

import androidx.compose.ui.graphics.Color
import io.silv.checkers.Blue
import io.silv.checkers.Cord
import io.silv.checkers.Empty
import io.silv.checkers.Piece
import io.silv.checkers.Red

fun Int.isEven() = this % 2 == 0

fun Int.isOdd() = this % 2 != 0

fun Cord.spaceBgColor(): Color {
    val (i, j) = this
    return when(val startWithBlack = i.isOdd()) {
        j.isEven() -> Color.Black
        else -> Color.White
    }
}

fun generateInitialBoard() = List(8) { i ->
    val startWithPiece = i.isOdd()
    val piece = getPiece(i)
    List(8) { j ->
        when {
            startWithPiece && j.isEven() -> piece
            !startWithPiece && j.isOdd() -> piece
            else ->  Empty
        }
    }
}

fun getPiece(i: Int): Piece {
    return when (i) {
        in (0..2) -> Red()
        in (5..7) -> Blue()
        else -> Empty
    }
}