package io.silv.checkers.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.google.firebase.auth.FirebaseUser
import io.silv.checkers.navigation.logged_in.LoggedIn
import io.silv.checkers.navigation.logged_out.LoggedOut
import kotlinx.parcelize.Parcelize

sealed class MainNavTarget : Parcelable {

    @Parcelize
    object LoggedOut : MainNavTarget()

    @Parcelize
    class LoggedIn(val user: FirebaseUser) : MainNavTarget()
}


class RootNode(
    buildContext: BuildContext,
    initialElement: MainNavTarget,
    private val backStack: BackStack<MainNavTarget> = BackStack(
        initialElement = initialElement,
        savedStateMap = buildContext.savedStateMap
    ),
    private val logOut: () -> Unit
): ParentNode<MainNavTarget>(backStack, buildContext) {

    // Here we map BackStack nav targets to the child Nodes
    override fun resolve(navTarget: MainNavTarget, buildContext: BuildContext): Node =
        when (navTarget) {
            is MainNavTarget.LoggedOut -> LoggedOut(buildContext) { user ->
                backStack.push(MainNavTarget.LoggedIn(user))
            }
            is MainNavTarget.LoggedIn -> LoggedIn(buildContext) {
                logOut()
                backStack.replace(MainNavTarget.LoggedOut)
            }
        }


    @Composable
    override fun View(modifier: Modifier) {

        Children(
            navModel = backStack,
            transitionHandler = rememberBackstackFader()
        )
    }
}