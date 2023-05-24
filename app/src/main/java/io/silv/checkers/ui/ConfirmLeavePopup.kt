package io.silv.checkers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Popup

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
            Modifier
                .fillMaxSize(0.6f)
                .background(Color.DarkGray)
        ) {
            Text(
                text = "Navigating back will cause the room to be deleted and you will need to recreate it"
            )
            Row {
                Button(
                    onClick = {
                        onConfirm()
                    }
                ) {
                    Text(text = "Confirm and delete room")
                }
                Button(
                    onClick = {
                        onDeny()
                    }
                ) {
                    Text(text = "Stay in queue")
                }
            }
        }
    }
}