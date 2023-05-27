package io.silv.checkers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.silv.checkers.Blue
import io.silv.checkers.Cord
import io.silv.checkers.DropData
import io.silv.checkers.Piece
import io.silv.checkers.Red
import io.silv.checkers.screens.Turn
import io.silv.checkers.ui.dragdrop.DragTarget
import io.silv.checkers.ui.dragdrop.DraggableContainer
import io.silv.checkers.ui.dragdrop.DropTarget
import io.silv.checkers.ui.dragdrop.LocalDragInfo
import io.silv.checkers.ui.dragdrop.spaceBgColor


@Composable
fun  CheckerBoard(
    modifier: Modifier = Modifier,
    board: List<List<Piece>>,
    onDropAction: (fromCord: Cord, toCord: Cord, piece: Piece) -> Unit
) {
    DraggableContainer(modifier) {

        Column(
            modifier = Modifier.aspectRatio(1f),
        ) {
           board.forEachIndexed { i, row ->
               Row(
                   Modifier.weight(1f)
               ) {
                   row.forEachIndexed { j, piece ->

                       val pos = i to j

                       CheckerSpace(
                           modifier = Modifier
                               .weight(1f)
                               .fillMaxHeight(),
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
    ) { _, data: DropData? ->

        data?.let { (fromIdx, piece) ->
            dropEvent(fromIdx, piece)
        }
        
        when (piece) {
            is Red, is Blue -> CircleTarget(
                data = gridPos to piece,
                color = if (dragInfo.draggedCord == gridPos) {
                    Color.Yellow
                } else {
                    piece.color
                },
                crowned = piece.crowned,
                modifier = Modifier
                    .fillMaxSize(0.7f)
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
    color: Color,
    crowned: Boolean
) {


    DragTarget(
        modifier = modifier,
        dataToDrop = data,
        gridPos = data.first
    ) {
        Box(modifier = Modifier.size(35.dp), contentAlignment = Alignment.Center) {
            Circle(color)
            if (crowned) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription ="crown",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}