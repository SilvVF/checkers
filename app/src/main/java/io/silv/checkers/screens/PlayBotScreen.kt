package io.silv.checkers.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.viewmodels.PlayBotViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayBotScreen(
    playBotViewModel: PlayBotViewModel = koinViewModel()
) {

    val board by playBotViewModel.board.collectAsState()

    CheckerBoard(
        modifier = Modifier.fillMaxSize(),
        board = board,
        onDropAction = { from, to, piece ->
            playBotViewModel.onDropAction(board, from, to, piece)
        }
    )
}