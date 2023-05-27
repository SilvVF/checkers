package io.silv.checkers.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.Room
import io.silv.checkers.firebase.deleteRoomCallbackFlow
import io.silv.checkers.firebase.joinRoom
import io.silv.checkers.firebase.roomsFlow
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
    private val db: DatabaseReference,
    auth: FirebaseAuth
): ViewModel() {

    private val userId = auth.currentUser?.uid ?: ""
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    private val _connecting = MutableStateFlow(false)
    val connecting = _connecting.asStateFlow()

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())

    init {
        viewModelScope.launch {
            db.roomsFlow().collect { roomList ->
                _rooms.emit(
                    roomList.sortedByDescending { room -> room.createdAtEpochSecond }
                )
            }
        }
    }

    suspend fun connectToRoom(roomId: String): String? {
        if (connecting.value) { return null }
        return runCatching {
            _connecting.emit(true)
            db.joinRoom(roomId, userId)
                .catch {
                    it.printStackTrace()
                }
                .first()
            roomId
        }
            .getOrNull()
            .also {
                _connecting.emit(false)
            }
    }

    @OptIn(FlowPreview::class)
    val rooms = combine(
        _query.debounce(400),
        _rooms
    ) { query, rooms ->
        rooms.filter {
            it.usersToColorChoice.size <= 1 &&
                    (it.id.contains(query, true) || it.name.contains(query, true))
        }.map {
            it.toUiRoom()
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun onQueryChanged(text: String) = viewModelScope.launch {
        _query.emit(text)
    }
}