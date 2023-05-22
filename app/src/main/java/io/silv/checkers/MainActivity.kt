package io.silv.checkers

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.firebase.roomStateFlow
import io.silv.checkers.screens.AuthScreen
import io.silv.checkers.screens.CreateRoomScreen
import io.silv.checkers.screens.SearchRoomScreen
import io.silv.checkers.ui.theme.DragDropTestTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


class MainActivityViewModel(
    private val auth: FirebaseAuth,
    private val db: DatabaseReference
): ViewModel() {

    val user = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val roomId = MutableStateFlow<String?>(null)

    val authed = user.map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val roomState = roomId.map {
        it?.let {
            db.roomStateFlow(it)
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)


    fun signIn(token: String) = viewModelScope.launch {
        val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnSuccessListener { task ->
                user.tryEmit(auth.currentUser)
            }
            .addOnFailureListener { e ->
                user.tryEmit(null)
            }
    }

    fun signOut() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        signOut()
    }
}

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {

            val vm: MainActivityViewModel = koinViewModel()

            val authed by vm.authed.collectAsState()

            val context = LocalContext.current

            DragDropTestTheme {
                Scaffold(
                    Modifier.fillMaxSize()
                ) { paddingValues ->
                    NavHost(
                        startDestination = "search",
                        navController = rememberNavController()
                    ) {
                        composable("search") {
                            SearchRoomScreen(paddingValues = paddingValues)
                        }
                        composable("create") {
                            CreateRoomScreen(
                                paddingValues = paddingValues,
                                showSnackBar = { reason ->
                                    Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
                                }
                            ) { roomId ->
                                Toast.makeText(context, roomId,Toast.LENGTH_SHORT).show()
                            }
                        }
                        composable("auth") {
                            AuthScreen(
                                paddingValues = paddingValues
                            ) { token, credentials ->

                            }
                        }
                    }
                }
            }
        }
    }
}





