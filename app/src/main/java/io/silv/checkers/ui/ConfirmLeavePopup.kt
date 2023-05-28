package io.silv.checkers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import io.silv.checkers.ui.theme.PrimaryGreen

@Composable
fun ConfirmLeavePopup(
    show: Boolean,
    onConfirm: () -> Unit,
    onDeny: () -> Unit
) {
    if (!show) { return }
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = { onDeny() }
    ) {
        Column(
            modifier = Modifier
                .background(Color.DarkGray)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Navigating back will cause the room to be deleted and you will need to recreate it"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                Button(
                    onClick = {
                        onConfirm()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp),
                    colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(text = "delete room")
                }
                Button(
                    onClick = {
                        onDeny()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                ) {
                    Text(text = "stay")
                }
            }
        }
    }
}