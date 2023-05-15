package io.silv.checkers

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import io.silv.checkers.ui.Blue
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.ui.Cord
import io.silv.checkers.ui.DropData
import io.silv.checkers.ui.Empty
import io.silv.checkers.ui.Piece
import io.silv.checkers.ui.Red
import io.silv.checkers.ui.dragdrop.DragTarget
import io.silv.checkers.ui.theme.DragDropTestTheme


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
        in (0..2) -> Red
        in (5..7) -> Blue
        else -> Empty
    }
}

class DragTargetInfo {
    var isDragging: Boolean by mutableStateOf(false)
    var draggedCord: Cord? by mutableStateOf(null)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggableComposable by mutableStateOf<(@Composable () -> Unit)?>(null)
    var dataToDrop by mutableStateOf<Any?>(null)
}

val LocalDragInfo = compositionLocalOf { DragTargetInfo() }

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
                LaunchedEffect(key1 = true) {
                    Log.d("BOARD", board.toString())
                }

                BackHandler {
                    // stop dragging from closing the app
                }

                CheckerBoard(
                    board = board,
                    onDropAction = { fromCord, toCord, piece ->
                        if (fromCord == toCord || board[toCord.first][toCord.second] != Empty) {
                            return@CheckerBoard
                        }
                        board = List(8) { i ->
                            List(8) { j ->
                                when (i to j) {
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



@Composable
fun Circle(color: Color) = Box(
    modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
        .background(color)
)

@Composable
fun CircleTarget(
    modifier: Modifier = Modifier,
    data: DropData,
    color: Color
) {


    DragTarget(
        modifier = modifier,
        dataToDrop = data,
        gridPos = data.first
    ) {
        Circle(color)
    }
}




