package io.silv.checkers.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.silv.checkers.usecase.DeleteRoomUseCase
import io.silv.checkers.usecase.GetUserInfoUseCase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val auth: FirebaseAuth,
    private val deleteRoomUseCase: DeleteRoomUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
): ViewModel() {

    val user = MutableStateFlow(auth.currentUser)

    fun signIn(token: String) = callbackFlow {
        Log.d("SIGN", "called $token")
        val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnSuccessListener {
               trySend(it.user)
            }
            .addOnFailureListener {
                it.printStackTrace()
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

    fun logOut() = viewModelScope.launch {
        getUserInfoUseCase(auth.currentUser?.uid ?: "")
            .onSuccess { user ->
                deleteRoomUseCase(user.joinedRoomId)
            }
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
    }
}