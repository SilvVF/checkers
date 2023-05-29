package io.silv.checkers.navigation.logged_in

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import io.silv.checkers.navigation.game.CheckersGame
import io.silv.checkers.navigation.logged_in.nodes.CreateRoom
import io.silv.checkers.navigation.logged_in.nodes.SearchRooms
import io.silv.checkers.navigation.logged_out.nodes.PlayBot
import io.silv.checkers.ui.AnimatedNavIcon
import io.silv.checkers.ui.dragdrop.DraggableContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LoggedIn(
    buildContext: BuildContext,
    private val backStack: BackStack<LoggedInNavTarget> = BackStack(
        initialElement = LoggedInNavTarget.SearchRooms,
        savedStateMap = buildContext.savedStateMap
    ),
    private val signOut: () -> Unit
): ParentNode<LoggedInNavTarget>(backStack, buildContext) {

    override fun resolve(navTarget: LoggedInNavTarget, buildContext: BuildContext): Node =
        when(navTarget) {
            is LoggedInNavTarget.CheckersGame -> CheckersGame(buildContext, navTarget.roomId) {
                backStack.replace(
                    LoggedInNavTarget.SearchRooms
                )
            }
            LoggedInNavTarget.CreateRoom -> CreateRoom(buildContext) { roomId ->
                backStack.push(
                    LoggedInNavTarget.CheckersGame(roomId)
                )
            }
            LoggedInNavTarget.SearchRooms -> SearchRooms(buildContext) { roomId ->
                backStack.push(
                    LoggedInNavTarget.CheckersGame(roomId)
                )
            }
            LoggedInNavTarget.PlayBot -> PlayBot(buildContext)
        }

    private val onScreen = flow {
        while (true) {
            delay(10)
            emit(backStack.activeElement)
        }
    }
        .flowOn(Dispatchers.Default)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun View(modifier: Modifier) {

        val onScreen by onScreen.collectAsState(initial = LoggedInNavTarget.SearchRooms)
        DraggableContainer {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        title = {},
                        navigationIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { signOut() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "sign out",
                                        tint = Color.LightGray
                                    )
                                }
                                Text(text = "sign out", color = Color.LightGray)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xff18181b)
                        )
                    )
                },
                bottomBar = {
                    if (onScreen is LoggedInNavTarget.CheckersGame) {
                        return@Scaffold
                    }
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AnimatedNavIcon(
                                icon = Icons.Default.Search,
                                contentDescription = "Search",
                                selected = onScreen is LoggedInNavTarget.SearchRooms,
                                onClick = {
                                    backStack.push(LoggedInNavTarget.SearchRooms)
                                }
                            )
                            AnimatedNavIcon(
                                icon = Icons.Default.Add,
                                contentDescription = "Add",
                                onClick = {
                                    backStack.push(LoggedInNavTarget.CreateRoom)
                                },
                                selected = onScreen is LoggedInNavTarget.CreateRoom,
                            )
                            AnimatedNavIcon(
                                icon = Icons.Default.PlayArrow,
                                contentDescription = "Play Bot",
                                selected = onScreen is LoggedInNavTarget.PlayBot
                            ) {
                                backStack.push(LoggedInNavTarget.PlayBot)
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
}