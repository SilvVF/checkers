package io.silv.checkers.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.silv.checkers.Blue
import io.silv.checkers.Board
import io.silv.checkers.Cord
import io.silv.checkers.Empty
import io.silv.checkers.Piece
import io.silv.checkers.Red
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.viewmodels.CheckerUiState
import io.silv.checkers.viewmodels.CheckersViewModel
import org.koin.androidx.compose.koinViewModel

fun Turn.correctPieceForTurn(piece: Piece): Boolean {
    return when (this) {
        Turn.Blue -> piece is Blue
        Turn.Red -> piece is Red
    }
}

enum class Turn(val value: Int) {
    Blue(2),
    Red(1)
}


@Composable
fun CheckersScreen(
    state: CheckerUiState,
    onDropAction: (from: Cord, to: Cord, piece: Piece) -> Unit
) {

    BackHandler {
        // stop dragging from closing the app
    }
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CheckerBoard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            board = state.board,
            onDropAction = { fromCord, toCord, piece ->
                onDropAction(fromCord, toCord, piece)
            }
        )
        Text(text = "time to move ${state.timeToMove}")
        Text(text = "turn ${state.turn}")
        Text(text = "my turn ${state.turnMe}")
        Text(text = "my piece ${state.playerPiece}")
    }
    

}