package io.silv.checkers.navigation

import android.os.Parcelable
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseUser
import io.silv.checkers.screens.AuthScreen
import io.silv.checkers.viewmodels.MainActivityViewModel
import kotlinx.parcelize.Parcelize
import org.koin.androidx.compose.koinViewModel

sealed class MainNavTarget : Parcelable {

    @Parcelize
    object LoggedOut : MainNavTarget()

    @Parcelize
    class Checkers(val user: FirebaseUser?) : MainNavTarget()
}

class LoggedOut(
    buildContext: BuildContext,
    private val vm: MainActivityViewModel
) : Node(buildContext) {



    @Composable
    override fun View(modifier: Modifier) {
        Scaffold { paddingValues ->
            AuthScreen(paddingValues = paddingValues) { token, _ ->
                vm.signIn(token)
            }
        }
    }
}

class RootNode(
    buildContext: BuildContext,
    initialElement: MainNavTarget,
    private val viewModel: MainActivityViewModel,
    private val backStack: BackStack<MainNavTarget> = BackStack(
        initialElement = initialElement,
        savedStateMap = buildContext.savedStateMap
    ),
): ParentNode<MainNavTarget>(backStack, buildContext) {

    // Here we map BackStack nav targets to the child Nodes
    override fun resolve(navTarget: MainNavTarget, buildContext: BuildContext): Node =
        when (navTarget) {
            is MainNavTarget.LoggedOut -> LoggedOut(buildContext, viewModel)
            is MainNavTarget.Checkers -> Checkers(
                buildContext
            )
        }


    @Composable
    override fun View(modifier: Modifier) {

        val user by viewModel.user.collectAsState()

        LaunchedEffect(user) {
            user?.let {
                backStack.push(
                    MainNavTarget.Checkers(it)
                )
            }
        }

        Children(
            navModel = backStack,
            transitionHandler = rememberBackstackFader()
        )
    }
}