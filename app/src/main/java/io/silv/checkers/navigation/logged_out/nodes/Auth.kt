package io.silv.checkers.navigation.logged_out.nodes

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.google.firebase.auth.FirebaseUser
import io.silv.checkers.screens.AuthScreen
import io.silv.checkers.viewmodels.MainActivityViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class Auth(
    buildContext: BuildContext,
    private val onAuthed: (firebaseUser: FirebaseUser) -> Unit
): Node(buildContext) {

    @Composable
    override fun View(modifier: Modifier) {

        val viewModel: MainActivityViewModel = koinViewModel()
        val scope = rememberCoroutineScope()

        AuthScreen(modifier = modifier) { token, credential ->
            scope.launch {
                Log.d("AUTHED scope call", "called")
                viewModel.signIn(token, credential)
                    .catch {
                        it.printStackTrace()
                    }
                    .first()?.let {
                        Log.d("AUTHED scope call", it.toString())
                        onAuthed(it)
                    }
            }
        }
    }
}