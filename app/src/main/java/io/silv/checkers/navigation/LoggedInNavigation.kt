package io.silv.checkers.navigation

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.google.android.gms.auth.api.identity.SignInCredential
import io.silv.checkers.screens.CheckersScreen
import io.silv.checkers.screens.CreateRoomScreen
import io.silv.checkers.screens.SearchRoomScreen
import kotlinx.parcelize.Parcelize

sealed class LoggedInNavTarget: Parcelable {

    @Parcelize
    object SearchRooms: LoggedInNavTarget()

    @Parcelize
    object CreateRoom: LoggedInNavTarget()

    @Parcelize
    data class CheckersGame(val roomId: String?): LoggedInNavTarget()
}

class CheckersGame(
    buildContext: BuildContext,
    val roomId: String?
): Node(buildContext = buildContext) {

    @Composable
    override fun View(modifier: Modifier) {
        CheckersScreen()
    }
}
class CreateRoom(
    buildContext: BuildContext,
): Node(buildContext = buildContext) {

    @Composable
    override fun View(modifier: Modifier) {
        CreateRoomScreen(
            showSnackBar = {

            },
            roomCreated = {

            }
        )
    }
}

class SearchRooms(
    buildContext: BuildContext,
): Node(buildContext = buildContext) {

    @Composable
    override fun View(modifier: Modifier) {
        SearchRoomScreen()
    }
}


class Checkers(
    buildContext: BuildContext,
    token: String,
    credential: SignInCredential?,
    private val backStack: BackStack<LoggedInNavTarget> = BackStack(
        initialElement = LoggedInNavTarget.SearchRooms,
        savedStateMap = buildContext.savedStateMap
    ),
): ParentNode<LoggedInNavTarget>(backStack, buildContext) {

    override fun resolve(navTarget: LoggedInNavTarget, buildContext: BuildContext): Node =
        when(navTarget) {
            is LoggedInNavTarget.CheckersGame -> CheckersGame(buildContext, navTarget.roomId)
            LoggedInNavTarget.CreateRoom -> CreateRoom(buildContext)
            LoggedInNavTarget.SearchRooms -> SearchRooms(buildContext)
        }
    @Composable
    override fun View(modifier: Modifier) {
            Scaffold(Modifier.fillMaxSize()) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xff27272a),
                                    Color(0xff18181b),
                                )
                            )
                        )
                        .padding(paddingValues)
                ) {
                    Children(
                        navModel = backStack,
                        transitionHandler = rememberBackstackFader()
                    )
                }
            }
    }
}