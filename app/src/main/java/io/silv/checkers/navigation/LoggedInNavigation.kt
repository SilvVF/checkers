package io.silv.checkers.navigation

import android.os.Parcelable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.flowWithLifecycle
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.composable.visibleChildrenAsState
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.active
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseUser
import io.silv.checkers.screens.CheckersScreen
import io.silv.checkers.screens.CreateRoomScreen
import io.silv.checkers.screens.SearchRoomScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
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

    private val onScreen = flow {
        while (true) {
            kotlinx.coroutines.delay(10)
            emit(backStack.activeElement)
        }
    }
        .flowOn(Dispatchers.Default)

    @Composable
    override fun View(modifier: Modifier) {

        val onScreen by onScreen.collectAsState(initial = LoggedInNavTarget.SearchRooms)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = {
                                backStack.push(LoggedInNavTarget.SearchRooms)
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(
                                    animateDpAsState(
                                        targetValue = if (onScreen is LoggedInNavTarget.SearchRooms) {
                                            52.dp
                                        } else {
                                            22.dp
                                        }
                                    ).value
                                ),
                                tint = animateColorAsState(
                                    targetValue = if (onScreen is LoggedInNavTarget.SearchRooms) {
                                        Color(0xff64C88D)
                                    } else {
                                        Color.DarkGray
                                    }
                                ).value,
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(
                            modifier = Modifier,
                            onClick = { backStack.push(LoggedInNavTarget.CreateRoom) }
                        ) {
                            Icon(
                                modifier = Modifier.size(
                                    animateDpAsState(
                                        targetValue = if (onScreen is LoggedInNavTarget.CreateRoom) {
                                            52.dp
                                        } else {
                                            22.dp
                                        }
                                    ).value
                                ),
                                tint = animateColorAsState(
                                    targetValue = if (onScreen is LoggedInNavTarget.CreateRoom) {
                                        Color(0xff64C88D)
                                    } else {
                                        Color.DarkGray
                                    }
                                ).value,
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create"
                            )
                        }
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