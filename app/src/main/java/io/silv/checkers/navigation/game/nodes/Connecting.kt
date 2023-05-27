package io.silv.checkers.navigation.game.nodes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node

class Connecting(
    buildContext: BuildContext,
    private val roomId: String,
): Node(buildContext) {

    @Composable
    override fun View(modifier: Modifier) {

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SelectionContainer {
                Text(text = "connecting $roomId")
            }
        }
    }
}