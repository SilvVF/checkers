package io.silv.checkers

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.NodeComponentActivity
import io.silv.checkers.navigation.MainNavTarget
import io.silv.checkers.navigation.RootNode
import io.silv.checkers.screens.PlayBotScreen
import io.silv.checkers.ui.Particle
import io.silv.checkers.ui.theme.DragDropTestTheme
import io.silv.checkers.viewmodels.MainActivityViewModel
import kotlinx.coroutines.flow.first
import org.koin.androidx.compose.koinViewModel


class MainActivity : NodeComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {

            @VisibleForTesting
            val vm: MainActivityViewModel = koinViewModel()

            val user = vm.user.collectAsState().value

            DragDropTestTheme {
                NodeHost(integrationPoint = appyxIntegrationPoint) {
                    RootNode(
                        initialElement = if (user != null) {
                            MainNavTarget.Checkers(user)
                        } else {
                            MainNavTarget.LoggedOut
                        },
                        buildContext = it,
                        viewModel = vm
                    )
                }
            }
        }
    }
}





