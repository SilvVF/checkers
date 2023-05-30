package io.silv.checkers.navigation.game.nodes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.silv.checkers.Blue
import io.silv.checkers.Cord
import io.silv.checkers.Piece
import io.silv.checkers.Red
import io.silv.checkers.screens.CheckersScreen
import io.silv.checkers.ui.util.collectEvents
import io.silv.checkers.usecase.generateInitialBoard
import io.silv.checkers.viewmodels.CheckerUiState
import io.silv.checkers.viewmodels.CheckersEvent
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
        val ctx = LocalContext.current

        viewModel.collectEvents { event ->
            when (event) {
                is CheckersEvent.FailedToDeleteRoom -> Toast.makeText(
                    ctx,event.reason, Toast.LENGTH_SHORT
                ).show()
                is CheckersEvent.MoveFailed ->  Toast.makeText(
                    ctx,event.reason, Toast.LENGTH_SHORT
                ).show()
            }
        }

        val state by viewModel.uiState.collectAsState()

        CheckersScreen(
            state,
            lastJumpEnd = viewModel.afterJumpEnd,
            endTurn = viewModel::endTurn
        ) { from: Cord, to: Cord, p: Piece ->
            viewModel.onDropAction(from, to, p)
        }
    }
}

@Preview
@Composable
fun PreviewCheckerScreen() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        Scaffold(
            Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0xff27272a),
                                Color(0xff18181b),
                            )
                        )
                    )
                    .padding(paddingValues)
            ) {
                CheckersScreen(
                    state = CheckerUiState(
                        board = generateInitialBoard(),
                        playerPiece = Blue(),
                        turnPiece = Red(),
                        turnMe = false,
                        timeToMove = 20,
                        turnsMatch = true,
                        winner = Blue()
                    ),
                    onDropAction = { _, _, _ -> },
                    endTurn = {},
                    lastJumpEnd = null
                )
            }
        }
    }
}

