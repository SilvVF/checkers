package io.silv.checkers.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.firebase.createRoomFlow
import io.silv.checkers.usecase.formatTime
import io.silv.checkers.ui.util.EventsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CreateRoomViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val db: DatabaseReference,
    private val auth: FirebaseAuth
): EventsViewModel<CreateRoomEvent>() {

    private val _sliderPosition = MutableStateFlow(30f)
    private val _creatingRoom = MutableStateFlow(false)
    val creatingRoom = _creatingRoom.asStateFlow()
    val sliderPosition = _sliderPosition.asStateFlow()
    val sliderText = _sliderPosition.map {
        formatTime(it.roundToInt())
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "30 seconds")


    fun changeSliderPosition(position: Float) = viewModelScope.launch {
        _sliderPosition.emit(
            ((position / 5).roundToInt() * 5f).coerceIn(30f..300f)
        )
    }

    suspend fun createRoom(name: String, color: Int): String?  {
        return runCatching {
//            val userId = auth.currentUser?.uid TODO()
//                ?: throw IllegalStateException("current user not signed in")
            db.createRoomFlow(name, color, "user", sliderPosition.value.roundToInt()).first()
        }
            .onSuccess { Log.d("ROOM", it) }
            .onFailure {
                it.printStackTrace()
                Log.d("ROOM", it.localizedMessage ?: "error")
                eventChannel.send(
                    CreateRoomEvent.CreateRoomError(it.localizedMessage ?: "Unknown Error")
                )
            }
            .getOrNull()
    }
}

sealed interface CreateRoomEvent {
    data class CreateRoomError(val reason: String): CreateRoomEvent
}

