package io.silv.checkers.navigation.logged_in.nodes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.silv.checkers.screens.CreateRoomScreen

class CreateRoom(
    buildContext: BuildContext,
    private val roomCreated: (roomId: String) -> Unit
): Node(buildContext = buildContext) {

    @Composable
    override fun View(modifier: Modifier) {
        CreateRoomScreen(
            showSnackBar = {

            },
            roomCreated = roomCreated
        )
    }
}
