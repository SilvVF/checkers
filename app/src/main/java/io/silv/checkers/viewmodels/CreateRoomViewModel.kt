package io.silv.checkers.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.firebase.createRoomFlow
import io.silv.checkers.usecase.formatTime
import io.silv.checkers.ui.util.EventsViewModel
import io.silv.checkers.usecase.CreateRoomUseCase
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
    auth: FirebaseAuth,
    private val createRoomUseCase: CreateRoomUseCase
): EventsViewModel<CreateRoomEvent>() {

    private val userId = auth.currentUser?.uid ?: ""
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
       return createRoomUseCase(
           name = name,
           color = color,
           userId = userId,
           moveTime = sliderPosition.value.roundToInt()
       )
           .onFailure {
               eventChannel.send(
                   CreateRoomEvent.CreateRoomError(
                       it.localizedMessage ?: "Unknown Error"
                   )
               )
           }
           .getOrNull()
    }
}

sealed interface CreateRoomEvent {
    data class CreateRoomError(val reason: String): CreateRoomEvent
}

