package io.silv.checkers.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import io.silv.checkers.Piece
import io.silv.checkers.Red
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.viewmodels.PlayBotViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayBotScreen(
    modifier: Modifier,
    playBotViewModel: PlayBotViewModel = koinViewModel()
) {

    val board by playBotViewModel.board.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CheckerBoard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            board = board,
            onDropAction = { from, to, piece ->
                playBotViewModel.onDropAction(board, from, to, piece)
            }
        )
        Row(Modifier.fillMaxHeight(0.3f)) {
            CheckersRemaining(
                modifier = Modifier
                    .padding(20.dp)
                    .weight(1f)
                    .fillMaxHeight(),
                board = board,
                text = "Blue Checkers Remaining",
                piece = Blue()
            )
            CheckersRemaining(
                modifier = Modifier
                    .padding(20.dp)
                    .weight(1f)
                    .fillMaxHeight(),
                board = board,
                text = "Red Checkers Remaining",
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
        Text(text = text)
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