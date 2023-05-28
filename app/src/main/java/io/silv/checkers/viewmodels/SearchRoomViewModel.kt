package io.silv.checkers.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.Room
import io.silv.checkers.UiRoom
import io.silv.checkers.firebase.deleteRoomCallbackFlow
import io.silv.checkers.firebase.joinRoom
import io.silv.checkers.firebase.roomsFlow
import io.silv.checkers.usecase.ConnectToRoomUseCase
import io.silv.checkers.usecase.GetJoinableRoomsFlowUseCase
import io.silv.checkers.usecase.toUiRoom
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchRoomViewModel(
    auth: FirebaseAuth,
    private val getJoinableRoomsFlowUseCase: GetJoinableRoomsFlowUseCase,
    private val connectToRoomUseCase: ConnectToRoomUseCase
): ViewModel() {

    private val userId = auth.currentUser?.uid ?: ""
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    private val _connecting = MutableStateFlow(false)
    val connecting = _connecting.asStateFlow()

    private val _rooms = MutableStateFlow<List<UiRoom>>(emptyList())

    init {
        viewModelScope.launch {
            getJoinableRoomsFlowUseCase().collect { roomList ->
                _rooms.emit(roomList)
            }
        }
    }

    suspend fun connectToRoom(roomId: String): String? {
        return connectToRoomUseCase(roomId, userId)
            .onFailure {
                it.printStackTrace()
            }
            .getOrNull()
    }

    @OptIn(FlowPreview::class)
    val rooms = combine(
        _query.debounce(400),
        _rooms
    ) { query, rooms ->
        rooms.filter {
            (it.id.contains(query, true) || it.name.contains(query, true))
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun onQueryChanged(text: String) = viewModelScope.launch {
        _query.emit(text)
    }
}