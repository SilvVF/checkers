package io.silv.checkers.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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


    Popup(
        alignment = Alignment.Center,
        onDismissRequest = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xff27272a)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally){
                Text(
                    text = "The winner of the game is",
                    color = Color.LightGray,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = piece.getString(),
                    color = piece.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen
                ),
                onClick = { onDismiss() }
            ) {
                Text(text = "go back to search")
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
