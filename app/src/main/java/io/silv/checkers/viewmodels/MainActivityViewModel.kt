package io.silv.checkers.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import io.silv.api.clientId
import io.silv.checkers.firebase.Fb
import io.silv.checkers.firebase.createUserFlow
import io.silv.checkers.firebase.roomStateFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val auth: FirebaseAuth,
): ViewModel() {

    val user = MutableStateFlow<FirebaseUser?>(auth.currentUser)

    fun signIn(token: String, credential: SignInCredential) = callbackFlow {
        Log.d("SIGN", "called $token")
        val firebaseCredential = GoogleAuthProvider.getCredential(token, clientId)
        auth.signInWithCredential(firebaseCredential)
            .addOnSuccessListener {
               trySend(it.user)
            }
            .addOnFailureListener {
                close(it)
            }
        awaitClose()
    }

    fun signOut() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        signOut()
    }
}