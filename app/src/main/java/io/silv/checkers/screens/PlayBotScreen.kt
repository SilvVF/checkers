package io.silv.checkers.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.silv.checkers.Blue
import io.silv.checkers.Piece
import io.silv.checkers.R
import io.silv.checkers.Red
import io.silv.checkers.getString
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.ui.theme.PrimaryGreen
import io.silv.checkers.viewmodels.PlayBotViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayBotScreen(
    modifier: Modifier,
    viewModel: PlayBotViewModel = koinViewModel()
) {

    val board by viewModel.board.collectAsState()
    
    BackHandler {
        // stop drag events from closing app
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        CheckerBoard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            board = board,
            onDropAction = { from, to, piece ->
                viewModel.onDropAction(board, from, to, piece)
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
        AnimatedVisibility(
            visible = viewModel.extraJumpsAvailable.collectAsState().value
        ) {
            Button(
                onClick = viewModel::changeTurn,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.LightGray
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f),
            ) {
                Text(
                    text = "End turn",
                    color= Color(0xff18181b),
                    fontSize = 17.sp
                )
            }
        }
        Button(
            onClick = viewModel::resetGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                contentColor = Color.LightGray
            ),
            modifier = Modifier
                .fillMaxWidth(0.6f),
        ) {
            Text(
                text = stringResource(id = R.string.reset),
                color= Color(0xff18181b),
                fontSize = 17.sp
            )
        }
        val pieceText = remember {
            buildAnnotatedString {
                val text = "your color is  "
                val pieceText = "Blue"
                append(text + pieceText)
                addStyle(
                    start = text.length,
                    end = text.length + pieceText.length,
                    style = SpanStyle(color = Blue().color)
                )
            }
        }
        Text(text = pieceText, color = Color.LightGray, fontSize = 22.sp ,modifier = Modifier.padding(12.dp))
        Row(Modifier.weight(0.3f)) {
            CheckersRemaining(
                modifier = Modifier
                    .padding(20.dp)
                    .weight(1f),
                board = board,
                text = stringResource(id = R.string.blue_checkers),
                piece = Blue()
            )
            CheckersRemaining(
                modifier = Modifier
                    .padding(20.dp)
                    .weight(1f),
                board = board,
                text = stringResource(id = R.string.red_checkers),
                piece = Red()
            )
        }
    }
}

@Composable
fun CheckersRemaining(
    modifier: Modifier,
    board: List<List<Piece>>,
    text: String,
    piece: Piece
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text, textAlign = TextAlign.Center, color = Color.LightGray,fontSize = 18.sp)
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
                text = remember(board) {
                    derivedStateOf {
                        board.sumOf { row ->
                            row.count { it.value == piece.value }
                        }
                            .toString()
                    }.value
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = remember {
                    derivedStateOf {
                        when(piece) {
                            is Red -> Color.Red
                            else -> Color.Blue
                        }
                    }.value
                }
            )
        }
    }
}