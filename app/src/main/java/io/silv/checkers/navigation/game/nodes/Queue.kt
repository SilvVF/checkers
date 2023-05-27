package io.silv.checkers.navigation.game.nodes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.silv.checkers.ui.ConfirmLeavePopup
import io.silv.checkers.viewmodels.CheckersViewModel
import org.koin.androidx.compose.koinViewModel

class Queue(
    buildContext: BuildContext,
    private val roomId: String,
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
            SelectionContainer {
                Text(text = "waiting for opponent ${state.room}")
            }
        }
    }
}