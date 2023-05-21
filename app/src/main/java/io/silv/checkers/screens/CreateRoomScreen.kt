package io.silv.checkers.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.Blue
import io.silv.checkers.Piece
import io.silv.checkers.R
import io.silv.checkers.Red
import io.silv.checkers.firebase.createRoomFlow
import io.silv.checkers.ui.util.EventsViewModel
import io.silv.checkers.ui.util.collectEvents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.IllegalStateException
import kotlin.math.roundToInt

class CreateRoomViewModel(
   private val savedStateHandle: SavedStateHandle,
   private val db: DatabaseReference,
   private val auth: FirebaseAuth
): EventsViewModel<CreateRoomEvent>() {

   private val _sliderPosition = MutableStateFlow(30f)
   val sliderPosition = _sliderPosition.asStateFlow()
   val sliderText = _sliderPosition.map {
      if (it <= 60) {
         "$it seconds"
      } else {
         var temp = it
         var minutes = 0
         while (temp >= 60) {
            temp -= 60
            minutes += 1
         }
         "$minutes minutes $temp seconds"
      }
   }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "30 seconds")


   fun changeSliderPosition(position: Float) = viewModelScope.launch {
      _sliderPosition.emit(
         ((position / 5).roundToInt() * 5f).coerceIn(30f..300f)
      )
   }

   suspend fun createRoom(name: String, color: Int): String? {
      runCatching {
         val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("current user not signed in")
         db.createRoomFlow(name, color, userId).first()
      }
         .onSuccess {
            return it
         }
         .onFailure {
            eventChannel.send(
               CreateRoomEvent.CreateRoomError(it.localizedMessage ?: "Unknown Error")
            )
         }
      return null
   }
}

sealed interface CreateRoomEvent {
   data class CreateRoomError(val reason: String): CreateRoomEvent
}


@Composable
fun CreateRoomScreen(
   viewModel: CreateRoomViewModel = koinViewModel(),
   paddingValues: PaddingValues,
   showSnackBar: (message: String) -> Unit,
   roomCreated: (roomId: String) -> Unit
) {

   viewModel.collectEvents {
      when (it) {
         is CreateRoomEvent.CreateRoomError -> showSnackBar(it.reason)
      }
   }

   val scope = rememberCoroutineScope()

   var roomName by rememberSaveable {
      mutableStateOf("")
   }

   var color by remember {
      mutableStateOf<Piece>(Red())
   }

   val sliderPosition by viewModel.sliderPosition.collectAsState()
   var creatingRoom by remember {
      mutableStateOf(false)
   }
   var focused by remember {
      mutableStateOf(false)
   }

   Column(
      horizontalAlignment = Alignment.CenterHorizontally,
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
         .padding(paddingValues)
   ) {
      Spacer(modifier = Modifier.height(22.dp))
      BasicTextField(
         value = roomName,
         onValueChange = { roomName = it },
         modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(70.dp)
            .onFocusChanged { focused = it.isFocused },
         singleLine = true,
         textStyle = MaterialTheme.typography.headlineMedium.copy(
            color = Color.LightGray
         ),
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
               if (roomName.isEmpty()) {
                  Text(
                     text = stringResource(id = R.string.room_name_hint),
                     color = Color.LightGray,
                     fontSize = 18.sp,
                  )
               }
               innerText()
            }
         }
      )
      Spacer(modifier = Modifier.height(22.dp))
      ColorSelector(
         colors = listOf(Red().color to "Red", Blue().color to "Blue"),
         selectedColor = color,
         modifier = Modifier
            .fillMaxWidth(0.9f)
            .sizeIn(minHeight = 200.dp, maxHeight = 225.dp)
            .weight(1f, false),
         onColorSelected = {
            color = when(it) {
               Red().color -> Red()
               else -> Blue()
            }
         }
      )
      Spacer(modifier = Modifier.height(22.dp))
      Column {
         Text(text = viewModel.sliderText.collectAsState().value, color = Color.LightGray)
         Slider(
            modifier = Modifier
               .semantics { contentDescription = "Localized Description" }
               .fillMaxWidth(0.8f),
            value = sliderPosition,
            onValueChange = { viewModel.changeSliderPosition(it) },
            valueRange = 30f..300f,
            onValueChangeFinished = {},
            colors = SliderDefaults.colors(
               thumbColor = Color(0xff64C88D),
               activeTrackColor = Color(0xff64C88D)
            ),
            steps = 60
         )
      }
      Text(
         text = "Time to make a move",
         color= Color.LightGray,
         fontSize = 22.sp
      )
      Spacer(modifier = Modifier.height(32.dp))
      Button(
         enabled = !creatingRoom,
         onClick = {
            if (creatingRoom) { return@Button }
            creatingRoom = true
            scope.launch {
               viewModel.createRoom(roomName, color.value)?.let { key ->
                  roomCreated(key)
               }
            }.invokeOnCompletion {
               creatingRoom = false
            }
         },
         colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xff64C88D),
            contentColor = Color.LightGray
         ),
         modifier = Modifier
            .imePadding(),
      ) {
         Text(
            text = "Create room with these settings",
            color= Color(0xff18181b),
            fontSize = 17.sp
         )
      }
   }
}

@Composable
fun ColorSelector(
   modifier: Modifier = Modifier,
   colors: List<Pair<Color, String>>,
   selectedColor: Piece,
   onColorSelected: (Color) -> Unit
) {


   Row(modifier) {
      colors.forEach { (c, name) ->

         val selected by remember(selectedColor, c) {
            derivedStateOf { selectedColor.color == c }
         }

         val borderColor by animateColorAsState(
            targetValue = if (selected) Color.LightGray else Color.Black
         )

         val circleColor by animateColorAsState(
            targetValue = if (selected) c else c.copy(alpha = 0.3f)
         )

         Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
         ) {
            Box(
               modifier = Modifier
                  .padding(12.dp)
                  .fillMaxWidth()
                  .weight(1f)
                  .clip(RoundedCornerShape(22.dp))
                  .background(
                     Brush.linearGradient(
                        if (selected) listOf(
                           Color(0xff64C88D),
                           Color(0xFF478D64),
                        )
                        else listOf(
                           Color(0xff171717),
                           Color(0xff262626),
                        )
                     )
                  )
                  .border(
                     color = borderColor,
                     width = 2.dp,
                     shape = RoundedCornerShape(22.dp)
                  )
                  .drawWithContent {
                     drawContent()
                     drawCircle(
                        color = circleColor,
                        radius = this.size.width / 3,
                        center = this.center
                     )
                  }
                  .clickable {
                     onColorSelected(c)
                  }
            )
            Text(text = name, color = circleColor)
         }
      }
   }
}