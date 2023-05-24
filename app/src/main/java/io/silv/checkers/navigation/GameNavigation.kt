package io.silv.checkers.navigation

import android.os.Parcelable
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Popup
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider
import io.silv.checkers.Cord
import io.silv.checkers.Piece
import io.silv.checkers.Room
import io.silv.checkers.screens.CheckersScreen
import io.silv.checkers.ui.ConfirmLeavePopup
import io.silv.checkers.viewmodels.CheckerUiState
import io.silv.checkers.viewmodels.CheckersViewModel
import kotlinx.parcelize.Parcelize
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

sealed class GameNavTarget: Parcelable {

    @Parcelize
    data class Connecting(val roomId: String): GameNavTarget()

    @Parcelize
    data class Queue(val roomId: String): GameNavTarget()

    @Parcelize
    data class Game(val roomId: String): GameNavTarget()
}

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

        val viewModel: CheckersViewModel = koinViewModel() { parametersOf(roomId) }

        val state by viewModel.uiState.collectAsState()

        LaunchedEffect(state.room.usersToColorChoice) {
            if (state.room.usersToColorChoice.size >= 2) {
                backStack.pop()
                backStack.push(GameNavTarget.Game(roomId))
            } else {
                backStack.pop()
                backStack.push(GameNavTarget.Queue(roomId))
            }
        }

        Children(
            navModel = backStack,
            transitionHandler = rememberBackstackSlider()
        )
    }
}


class Game(
    buildContext: BuildContext,
    val roomId: String,
): Node(buildContext) {


    @Composable
    override fun View(modifier: Modifier) {

        val viewModel: CheckersViewModel = koinViewModel()  { parametersOf(roomId) }

        val state by viewModel.uiState.collectAsState()

        CheckersScreen(state) { from: Cord, to: Cord, piece: Piece ->
            viewModel.onDropAction(state.room, state.rawBoard, state.board, from, to, piece)
        }
    }
}

class Queue(
    buildContext: BuildContext,
    val roomId: String,
    private val navigateBack: () -> Unit
): Node(buildContext) {

    @Composable
    override fun View(modifier: Modifier) {

        val viewModel: CheckersViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsState()

        var leaveConfirmationVisible by remember {
            mutableStateOf(false)
        }

        ConfirmLeavePopup(
            show = leaveConfirmationVisible,
            onConfirm = {
                viewModel.deleteRoom(roomId)
                navigateBack()
            },
            onDeny = {
                leaveConfirmationVisible = false
            }
        )

        BackHandler {
           leaveConfirmationVisible = true
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SelectionContainer() {
                Text(text = "waiting for opponent ${state.room}")
            }
        }
    }
}

class Connecting(
    buildContext: BuildContext,
    private val roomId: String,
): Node(buildContext) {

    @Composable
    override fun View(modifier: Modifier) {

        val viewModel: CheckersViewModel = koinViewModel()

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SelectionContainer {
                Text(text = "connecting $roomId")
            }
        }
    }
}