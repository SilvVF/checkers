package io.silv.checkers.navigation

import android.os.Parcelable
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.google.android.gms.auth.api.identity.SignInCredential
import io.silv.checkers.screens.AuthScreen
import kotlinx.parcelize.Parcelize

sealed class MainNavTarget : Parcelable {

    @Parcelize
    object LoggedOut : MainNavTarget()

    @Parcelize
    class Checkers(val token: String, val credential: SignInCredential?) : MainNavTarget()
}

class LoggedOut(buildContext: BuildContext) : Node(buildContext) {

    @Composable
    override fun View(modifier: Modifier) {
        Scaffold { paddingValues ->
            AuthScreen(paddingValues = paddingValues) { token, credential ->
                performUpNavigation()
            }
        }
    }
}

class RootNode(
    buildContext: BuildContext,
    private val backStack: BackStack<MainNavTarget> = BackStack(
        initialElement = MainNavTarget.Checkers("", null),
        savedStateMap = buildContext.savedStateMap
    ),
): ParentNode<MainNavTarget>(backStack, buildContext) {

    // Here we map BackStack nav targets to the child Nodes
    override fun resolve(navTarget: MainNavTarget, buildContext: BuildContext): Node =
        when (navTarget) {
            is MainNavTarget.LoggedOut -> LoggedOut(buildContext)
            is MainNavTarget.Checkers -> Checkers(
                buildContext, navTarget.token, navTarget.credential
            )
        }


    @Composable
    override fun View(modifier: Modifier) {
        Children(
            navModel = backStack,
            transitionHandler = rememberBackstackFader()
        )
    }
}