package io.silv.checkers.navigation.logged_out.nodes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.silv.checkers.screens.PlayBotScreen

class PlayBot(
    buildContext: BuildContext,
): Node(buildContext) {

    @Composable
    override fun View(modifier: Modifier) {
        PlayBotScreen(modifier)
    }
}

