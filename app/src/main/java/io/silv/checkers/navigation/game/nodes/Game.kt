package io.silv.checkers.navigation.game.nodes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.silv.checkers.Cord
import io.silv.checkers.Piece
import io.silv.checkers.screens.CheckersScreen
import io.silv.checkers.viewmodels.CheckersViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


class Game(
    buildContext: BuildContext,
    private val roomId: String,
): Node(buildContext) {

    @Composable
    override fun View(modifier: Modifier) {

        val viewModel: CheckersViewModel = koinViewModel  { parametersOf(roomId) }

        val state by viewModel.uiState.collectAsState()

        CheckersScreen(
            state,
            forceMove =  {}
        ) { from: Cord, to: Cord, p: Piece ->
            viewModel.onDropAction(from, to, p)
        }
    }
}