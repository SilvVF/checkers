package io.silv.checkers.navigation

import android.os.Parcelable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.activeElement
import com.bumble.appyx.navmodel.backstack.operation.push
import io.silv.checkers.screens.AuthScreen
import io.silv.checkers.screens.PlayBotScreen
import io.silv.checkers.ui.AnimatedNavIcon
import io.silv.checkers.viewmodels.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.parcelize.Parcelize
import org.koin.androidx.compose.koinViewModel

sealed class LoggedOutNavTarget: Parcelable {

    @Parcelize
    object PlayAi: LoggedOutNavTarget()

    @Parcelize
    object Auth: LoggedOutNavTarget()
}

class LoggedOut(
    buildContext: BuildContext,
    private val backStack: BackStack<LoggedOutNavTarget> = BackStack(
        initialElement = LoggedOutNavTarget.Auth,
        savedStateMap = buildContext.savedStateMap
    )
): ParentNode<LoggedOutNavTarget>(
    backStack,
    buildContext
) {
    override fun resolve(navTarget: LoggedOutNavTarget, buildContext: BuildContext): Node =
        when (navTarget) {
            LoggedOutNavTarget.Auth -> Auth(buildContext)
            LoggedOutNavTarget.PlayAi -> PlayAi(buildContext)
        }

    private val onScreen = flow {
        while (true) {
            delay(10)
            emit(backStack.activeElement)
        }
    }
        .flowOn(Dispatchers.Default)

    @Composable
    override fun View(modifier: Modifier) {

        val onScreen by onScreen.collectAsState(initial = LoggedOutNavTarget.Auth)

        Scaffold(
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AnimatedNavIcon(
                            icon = Icons.Default.AccountCircle,
                            contentDescription = "Log In",
                            selected = onScreen is LoggedOutNavTarget.Auth,
                            onClick =  {
                                backStack.push(LoggedOutNavTarget.Auth)
                            }
                        )
                        AnimatedNavIcon(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = "Play Ai",
                            onClick = {
                                backStack.push(LoggedOutNavTarget.PlayAi)
                            },
                            selected = onScreen is LoggedOutNavTarget.PlayAi,
                        )
                    }
                }
            }
        ) { paddingValues ->
            Children(
                navModel = backStack,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

class PlayAi(
    buildContext: BuildContext,
): Node(buildContext) {

    @Composable
    override fun View(modifier: Modifier) {
        PlayBotScreen(modifier)
    }
}

class Auth(
    buildContext: BuildContext,
): Node(buildContext) {

    @Composable
    override fun View(modifier: Modifier) {

        val viewModel: MainActivityViewModel = koinViewModel()

        AuthScreen(modifier = modifier) { token, _ ->
            viewModel.signIn(token)
        }
    }
}