package io.silv.checkers.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.silv.api.clientId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class MainActivityViewModel(
    private val auth: FirebaseAuth,
): ViewModel() {

    val user = MutableStateFlow(auth.currentUser)

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

    fun signInAnonymously() = callbackFlow {
        auth.signInAnonymously()
            .addOnSuccessListener {
                trySend(it.user)
            }
            .addOnFailureListener {
                close(it)
            }
        awaitClose()
    }

    fun logOut() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
    }
}