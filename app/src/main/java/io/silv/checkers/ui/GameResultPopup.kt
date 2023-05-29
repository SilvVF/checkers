package io.silv.checkers.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import io.silv.checkers.Piece
import io.silv.checkers.getString
import io.silv.checkers.ui.theme.PrimaryGreen

@Composable
fun GameResultPopup(
    piece: Piece,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) { return }

    val pieceText = remember {
        buildAnnotatedString {
            val text = "The winner of the game is "
            val pieceText = piece.getString()
            append(text + pieceText)
            addStyle(
                start = text.length,
                end = text.length + pieceText.length,
                style = SpanStyle(color = piece.color)
            )
        }
    }

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .padding(32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xff27272a)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = pieceText, color = Color.LightGray, fontWeight = FontWeight.SemiBold, fontSize = 40.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen
                ),
                onClick = { onDismiss() }
            ) {
                Text(text = "go back to search")
            }
        }
    }
}

class ParticleState(
    val color: Color,
    val endOffsetY: Dp,
    val endOffsetX: Dp
) {
    var offsetX by mutableStateOf(0.dp)
    var offsetY by mutableStateOf(0.dp)
    var scale by mutableStateOf(1f)
    var height by mutableStateOf(0.dp)
    var width by mutableStateOf(0.dp)
    var alpha by mutableStateOf(0f)

    fun update(progress: Float) {
        offsetX = endOffsetX * progress
        offsetY = endOffsetY * progress
        alpha = when(progress) {
            in 0.0f..0.8f -> 1f
            in 0.8f..0.85f -> 0.7f
            in 0.85f..0.9f -> 0.5f
            in 0.9f..0.95f -> 0.2f
            in 0.95f..0.99f -> 0.1f
            else -> 0f
        }
    }
}

@Composable
fun Particle(
    size: DpSize,
    endOffsetY: Dp,
    endOffsetX: Dp,
    color: Color
) {

    val state = remember {
        ParticleState(
            color = color,
            endOffsetY = endOffsetY,
            endOffsetX = endOffsetX
        )
    }

    val progress by animateFloatAsState(
        targetValue = 10000f,
        animationSpec = tween(10000)
    )

    LaunchedEffect(progress) {
        state.update(progress / 10000)
    }


    Box(
        Modifier
            .height(size.height)
            .width(size.width)
            .background(color)
            .offset(x = state.offsetX, y = state.offsetY)



    )
}
