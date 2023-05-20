package io.silv.checkers

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.silv.checkers.firebase.Fb
import io.silv.checkers.firebase.roomStateFlow
import io.silv.checkers.screens.AuthScreen
import io.silv.checkers.ui.dragdrop.generateInitialBoard
import io.silv.checkers.ui.theme.DragDropTestTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random



class MainActivityViewModel: ViewModel() {

    private val auth = Fb.auth
    private val db = Fb.database.database.reference

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


    fun createRoom(name: String = "") = viewModelScope.launch {
        val id = UUID.randomUUID().toString()
        roomId.emit(id)
        Room(
            id = id,
            name = name,
            users = listOf(user.value?.uid ?: "TestUser${Random.nextInt()}"),
            board = generateInitialBoard()
        )
            .pushToDb(db)
    }

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
        Fb.auth = Firebase.auth
        Fb.database = Firebase.database.reference

        val auth = Fb.auth

        setContent {

            val vm by viewModels<MainActivityViewModel>()

            val authed by vm.authed.collectAsState()

            LaunchedEffect(key1 = true) {
                vm.createRoom("Test33")
            }

            DragDropTestTheme {
                if (authed) {

                } else {
                    AuthScreen { t, c ->
                        vm.signIn(t)
                    }
                }
            }
        }
    }
}





