package io.silv.checkers.navigation

import android.os.Parcelable
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider
import io.silv.checkers.Room
import io.silv.checkers.screens.CheckersScreen
import io.silv.checkers.viewmodels.CheckerUiState
import io.silv.checkers.viewmodels.CheckersViewModel
import kotlinx.parcelize.Parcelize
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

sealed class GameNavTarget: Parcelable {

    @Parcelize
    data class Connecting(val roomId: String): GameNavTarget()

    @Parcelize
    data class Queue(val roomId: String, val room: Room): GameNavTarget()

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

        val state = viewModel.uiState.collectAsState().value

        LaunchedEffect(key1 = state) {
            when (state) {
                is CheckerUiState.Playing -> {
                    Log.d("FB", "called playing joinedRoom")
                    backStack.push(
                        GameNavTarget.Game(roomId)
                    )
                }
                is CheckerUiState.Queue -> {
                    Log.d("FB", "called playing joinedRoom")
                    backStack.push(
                        GameNavTarget.Queue(roomId, state.room)
                    )
                }
                else -> Unit
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

        when (val state = viewModel.uiState.collectAsState().value) {
            is CheckerUiState.Playing -> {
                CheckersScreen(state)
            }
            else -> {
                // TODO("Navigate back on error")
            }

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


        BackHandler {
            viewModel.deleteRoom(roomId)
            navigateBack()
        }

        when (val state = viewModel.uiState.collectAsState().value) {
            is CheckerUiState.Queue -> {
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
            else -> Unit
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
            SelectionContainer() {
                Text(text = "connecting $roomId")
            }
        }
    }
}