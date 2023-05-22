package io.silv.checkers.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import io.silv.checkers.R
import io.silv.checkers.ui.rememberIsImeVisible
import io.silv.checkers.viewmodels.SearchRoomViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRoomScreen(
    viewModel: SearchRoomViewModel = koinViewModel(),
) {

    val rooms by viewModel.rooms.collectAsState()
    val listState = rememberLazyListState()
    var focused by remember {
        mutableStateOf(false)
    }
    val imeVisible by rememberIsImeVisible()
    val jumpToTopVisible by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex >= 2 && !imeVisible
        }
    }
    val query by viewModel.query.collectAsState()
    val scope = rememberCoroutineScope()

    if(jumpToTopVisible) {
       Popup(
           alignment = Alignment.BottomEnd,
           IntOffset(0, -200)
       ) {
           Button(
               onClick = {
                scope.launch { listState.animateScrollToItem(0,) }
               },
               colors = ButtonDefaults.buttonColors(
                   containerColor = Color(0xff64C88D),
                   disabledContainerColor = Color(0xff262626)
               ),
           ) {
               Text(text = "Jump to top", color = Color(0xff262626))
           }
       }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        BasicTextField(
            value = query,
            onValueChange = { viewModel.onQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .onFocusChanged { focused = it.isFocused },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                color = Color.LightGray
            ),
            cursorBrush = SolidColor(Color.LightGray),
            decorationBox = { innerText ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .drawWithContent {
                            drawContent()
                            drawLine(
                                color = Color.LightGray,
                                start = Offset(0f, this.size.height),
                                end = Offset(this.size.width, this.size.height)
                            )
                        },
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.search_room_hint),
                            color = Color.LightGray,
                            fontSize = 18.sp,
                        )
                    }
                    innerText()
                }
            }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, true),
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
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(12.dp)) {
                        Text(text = room.name, color = Color(0xff64C88D))
                        Text(text = room.moveTime, color = Color.LightGray)
                        Text(text = room.dateCreated,color = Color.LightGray)
                    }
                }
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }
        }
    }
}
