package io.silv.checkers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import io.silv.checkers.ui.Blue
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.ui.CheckersScreen
import io.silv.checkers.ui.Empty
import io.silv.checkers.ui.Piece
import io.silv.checkers.ui.Red
import io.silv.checkers.ui.dragdrop.generateInitialBoard
import io.silv.checkers.ui.theme.DragDropTestTheme
import io.silv.checkers.validation.validatePlacement


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DragDropTestTheme {
                CheckersScreen()
            }
        }
    }
}





