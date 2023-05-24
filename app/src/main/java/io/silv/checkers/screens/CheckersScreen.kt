package io.silv.checkers.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.viewmodels.CheckerUiState

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
    forceMove: () -> Unit,
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
                .systemBarsPadding()
                .weight(1f, true),
            board = state.board,
            onDropAction = { fromCord, toCord, piece ->
                onDropAction(fromCord, toCord, piece)
            }
        )
        if (state.ableToForceOpponentMove) {
            Button(
                enabled = !state.moveInProgress,
                onClick = forceMove,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff64C88D),
                    contentColor = Color.LightGray
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .imePadding(),
            ) {
                Text(
                    text = "Don't wait for opponent",
                    color= Color(0xff18181b),
                    fontSize = 17.sp
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxHeight(0.2f)
        ) {
            MoveTimer(
                modifier = Modifier
                    .padding(20.dp)
                    .weight(1f)
                    .fillMaxHeight(),
                time = state.timeToMove,
                startTime = state.room.moveTimeSeconds
            )
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Current Turn")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = Color(0xff64C88D),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.turn.name,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = remember {
                            derivedStateOf {
                                when(state.turn.value) {
                                    Red().value -> Color.Red
                                    else -> Color.Blue
                                }
                            }.value
                        }
                    )
                }
            }
        }
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
        Text(text = "Time to move")
        AnimatedContent(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = Color(0xff64C88D),
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