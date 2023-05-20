package io.silv.checkers.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.silv.checkers.Room
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchRoomViewModel: ViewModel() {

    private val query = MutableStateFlow("")
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())

    init {
        viewModelScope.launch {

        }
    }

    @OptIn(FlowPreview::class)
    val rooms = combine(
        query.debounce(1000),
        _rooms
    ) { query, rooms ->
        rooms.filter {
            it.users.size <= 1 &&
            (it.id.contains(query, true) || it.name.contains(query, true))
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    fun onQueryChanged(text: String) = viewModelScope.launch {
        query.emit(text)
    }
}

@Composable
fun SearchRoomScreen(
    vm: SearchRoomViewModel
) {


}
