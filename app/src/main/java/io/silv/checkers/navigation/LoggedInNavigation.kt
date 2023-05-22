package io.silv.checkers.navigation

import android.os.Parcelable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.navmodel.backstack.operation.push
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
        val currentScreen by remember {
            derivedStateOf { backStack.activeElement }
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        modifier = Modifier.graphicsLayer {
                            if (currentScreen is LoggedInNavTarget) {
                                scaleX = 1.5f
                                scaleY = 1.5f
                            }
                        },
                        onClick = { backStack.push(LoggedInNavTarget.SearchRooms) }
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(
                        modifier = Modifier.graphicsLayer {
                            if (currentScreen is LoggedInNavTarget.CreateRoom) {
                                scaleX = 1.5f
                                scaleY = 1.5f
                            }
                        },
                        onClick = { backStack.push(LoggedInNavTarget.CreateRoom) }
                    ) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Create")
                    }
                }
            }
            ) { paddingValues ->
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