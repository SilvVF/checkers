package io.silv.checkers

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.NodeComponentActivity
import io.silv.checkers.navigation.MainNavTarget
import io.silv.checkers.navigation.RootNode
import io.silv.checkers.ui.theme.DragDropTestTheme
import io.silv.checkers.viewmodels.MainActivityViewModel
import org.koin.androidx.compose.koinViewModel


class MainActivity : NodeComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {

            @VisibleForTesting
            val vm: MainActivityViewModel = koinViewModel()

            val user by vm.user.collectAsState()

            DragDropTestTheme {
                NodeHost(integrationPoint = appyxIntegrationPoint) {
                    RootNode(
                        initialElement = if (user != null) {
                            MainNavTarget.LoggedIn(user!!)
                        } else {
                            MainNavTarget.LoggedOut
                        },
                        buildContext = it,
                    ) {
                        vm.logOut()
                    }
                }
            }
        }
    }
}





