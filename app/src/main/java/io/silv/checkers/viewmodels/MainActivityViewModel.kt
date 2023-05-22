package io.silv.checkers.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.firebase.roomStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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