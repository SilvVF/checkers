package io.silv.checkers.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.Room
import io.silv.checkers.firebase.roomsFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime
import java.time.ZoneOffset

class SearchRoomViewModel(
    val db: DatabaseReference
): ViewModel() {

    private val query = MutableStateFlow("")
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())

    init {
        viewModelScope.launch {
            db.roomsFlow().collect {
                _rooms.emit(it)
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRoomScreen(
    viewModel: SearchRoomViewModel = koinViewModel(),
    paddingValues: PaddingValues,
) {

    val rooms by  viewModel.rooms.collectAsState()
    val listState = rememberLazyListState()
    val jumpToTopVisible by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex >= 2
        }
    }
    val scope = rememberCoroutineScope()

    if(jumpToTopVisible) {
       Popup(alignment = Alignment.BottomCenter, IntOffset(0, -200)) {
           Button(onClick = {
               scope.launch { listState.animateScrollToItem(0,) }}) {
               Text(text = "Jump to top")
           }
       }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0xff27272a),
                        Color(0xff18181b),
                    )
                )
            )
            .padding(paddingValues),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        items(
            items = rooms,
            key = { room -> room.id }
        ) {room ->
            Card(
                onClick = {

                },
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xff262626)
                ),
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                    Text(room.name, fontSize = 22.sp, modifier= Modifier.padding(start =   12.dp))
                    Text(text = "move time ${room.moveTime}",Modifier.padding(start =   12.dp))
                    Text(
                        text = "created at ${LocalDateTime.ofEpochSecond(room.createdAt, 0, ZoneOffset.UTC)}",Modifier.padding(start =   12.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
