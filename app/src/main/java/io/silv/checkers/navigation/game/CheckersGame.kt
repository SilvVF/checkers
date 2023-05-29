package io.silv.checkers.navigation.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.replace
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider
import io.silv.checkers.navigation.game.nodes.Connecting
import io.silv.checkers.navigation.game.nodes.Game
import io.silv.checkers.navigation.game.nodes.Queue
import io.silv.checkers.ui.GameResultPopup
import io.silv.checkers.viewmodels.CheckersViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf



class CheckersGame(
    buildContext: BuildContext,
    private val roomId: String,
    private val backStack: BackStack<GameNavTarget> = BackStack(
        initialElement = GameNavTarget.Connecting(roomId),
        savedStateMap = buildContext.savedStateMap
    ),
    private val navigateBack: () -> Unit,
): ParentNode<GameNavTarget>(backStack ,buildContext) {

    override fun resolve(navTarget: GameNavTarget, buildContext: BuildContext): Node =
        when(navTarget) {
            is GameNavTarget.Connecting -> Connecting(buildContext, navTarget.roomId)
            is GameNavTarget.Game -> Game(buildContext, navTarget.roomId)
            is GameNavTarget.Queue -> Queue(buildContext, navTarget.roomId) {
                navigateBack()
            }
        }

    @Composable
    override fun View(modifier: Modifier) {

        val viewModel: CheckersViewModel = koinViewModel { parametersOf(roomId) }

        val state by viewModel.uiState.collectAsState()

        state.winner?.let {
            GameResultPopup(piece = it, visible = true) {
                viewModel.deleteRoom()
                navigateBack()
            }
        }

        LaunchedEffect(state.room.usersToColorChoice) {
            if (state.room.usersToColorChoice.size >= 2) {
                backStack.replace(GameNavTarget.Game(roomId))
            } else {
                backStack.replace(GameNavTarget.Queue(roomId))
            }
        }

        Children(
            navModel = backStack,
            transitionHandler = rememberBackstackSlider()
        )
    }
}
