package io.silv.checkers.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.silv.checkers.Blue
import io.silv.checkers.Cord
import io.silv.checkers.Piece
import io.silv.checkers.Red
import io.silv.checkers.getString
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.ui.theme.PrimaryGreen
import io.silv.checkers.viewmodels.CheckerUiState



@Composable
fun CheckersScreen(
    state: CheckerUiState,
    endTurn: () -> Unit,
    lastJumpEnd: Cord?,
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
                .weight(1f, true),
            board = state.board,
            onDropAction = { fromCord, toCord, piece ->
                onDropAction(fromCord, toCord, piece)
            }
        )
        AnimatedVisibility(visible = lastJumpEnd == null) {
            Row {
                Text(
                    text = "Your pieces are ",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.LightGray
                )
                Text(
                    text = state.playerPiece.getString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = state.playerPiece.color
                )
            }
        }
        lastJumpEnd?.let {
            Button(
                onClick = { endTurn() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen
                )
            ) {
                Text(text = "End turn", color = Color.LightGray)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
        ) {
            if (state.turnMe) {
                MoveTimer(
                    modifier = Modifier
                        .weight(1f)
                        .padding(20.dp),
                    time = state.timeToMove,
                    startTime = state.room.moveTimeSeconds
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Current Turn", color = Color.LightGray, fontSize = 18.sp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = PrimaryGreen,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.turnPiece.getString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = state.turnPiece.color
                    )
                }
            }
        }
        CheckersRemaining(modifier = Modifier
            .fillMaxWidth()
            .weight(0.4f), board = state.board)
    }
}

@Composable
fun CheckersRemaining(modifier: Modifier, board: List<List<Piece>>) {
    Row(modifier = modifier) {
        CheckersRemaining(
            modifier = Modifier
                .padding(20.dp)
                .weight(1f),
            board = board,
            text = "Blue Checkers",
            piece = Blue()
        )
        CheckersRemaining(
            modifier = Modifier
                .padding(20.dp)
                .weight(1f),
            board = board,
            text = "Red Checkers",
            piece = Red()
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MoveTimer(modifier: Modifier = Modifier, time: Int, startTime: Int) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Time to move", color = Color.LightGray, fontSize = 18.sp)
        AnimatedContent(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = PrimaryGreen,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center,
            targetState = time,
            transitionSpec = {
                slideInVertically { initialOffsetY ->
                    -initialOffsetY
                } with slideOutVertically { initialOffsetY ->
                    initialOffsetY
                }
            }
        ) { time ->
            Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
                val moveTimeSection by remember {
                    derivedStateOf { startTime / 3 }
                }
                Text(
                    text = "$time",
                    color = when(time) {
                        in 0.. moveTimeSection -> Color.Red
                        in moveTimeSection..(moveTimeSection * 2) -> Color.Yellow
                        else -> Color.White
                    },
                    fontSize = 32.sp
                )
            }
        }
    }
}