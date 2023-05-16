package io.silv.checkers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.silv.checkers.ui.dragdrop.Cord
import io.silv.checkers.ui.dragdrop.DragTarget
import io.silv.checkers.ui.dragdrop.DraggableContainer
import io.silv.checkers.ui.dragdrop.DropData
import io.silv.checkers.ui.dragdrop.DropTarget
import io.silv.checkers.ui.dragdrop.LocalDragInfo
import io.silv.checkers.ui.dragdrop.spaceBgColor

@Immutable
sealed class Piece(
    val value: Int,
    val color: Color,
)

object Empty: Piece(0, Color.Transparent)

data class Red(val crowned: Boolean = false): Piece(1, Color.Red)

data class Blue(val crowned: Boolean = false): Piece(2, Color.Blue)

@Composable
fun CheckerBoard(
    board: List<List<Piece>>,
    onDropAction: (fromCord: Cord, toCord: Cord, piece: Piece) -> Unit
) {
    DraggableContainer(Modifier.fillMaxSize()) {

        val dragInfo = LocalDragInfo.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
        ) {
           board.forEachIndexed { i, row ->
               Row(
                   Modifier.weight(1f)
               ) {
                   row.forEachIndexed { j, piece ->

                       val pos = i to j

                       CheckerSpace(
                           modifier = Modifier.weight(1f).fillMaxHeight(),
                           gridPos = pos,
                           piece = piece
                       ) { from, p ->
                           onDropAction(from, pos, p)
                       }
                   }
               }
            }
        }
    }
}


@Composable
fun CheckerSpace(
    modifier: Modifier = Modifier,
    gridPos: Cord,
    piece: Piece,
    dropEvent: (fromCord: Cord, piece: Piece) -> Unit
) {

    val dragInfo = LocalDragInfo.current

    DropTarget<DropData>(
        modifier = modifier.background(
            gridPos.spaceBgColor()
        ),
        gridPos = gridPos
    ) { isInBound, data ->

        data?.let { (fromIdx, piece) ->
            dropEvent(fromIdx, piece)
        }
        
        when (piece) {
            is Red, is Blue -> CircleTarget(
                data = gridPos to piece,
                color = if (dragInfo.draggedCord == gridPos) {
                    Color.Green
                } else {
                    piece.color
                },
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center)
            )
            else -> Unit
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