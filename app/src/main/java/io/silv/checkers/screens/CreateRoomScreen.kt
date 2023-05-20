package io.silv.checkers.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.Blue
import io.silv.checkers.Piece
import io.silv.checkers.R
import io.silv.checkers.Red
import io.silv.checkers.firebase.createRoomFlow
import io.silv.checkers.ui.util.EventsViewModel
import io.silv.checkers.ui.util.collectEvents
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.IllegalStateException

class CreateRoomViewModel(
   private val savedStateHandle: SavedStateHandle,
   private val db: DatabaseReference,
   private val auth: FirebaseAuth
): EventsViewModel<CreateRoomEvent>() {


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
         textStyle = MaterialTheme.typography.headlineLarge,
         decorationBox = { innerText ->
            Row(
               modifier = Modifier
                  .fillMaxSize()
                  .clip(RoundedCornerShape(12.dp))
                  .border(width = 1.dp, color = Color.Black, RoundedCornerShape(12.dp))
                  .padding(4.dp),
               horizontalArrangement = Arrangement.Start,
               verticalAlignment = Alignment.CenterVertically
            ) {
               if (!focused && roomName.isEmpty()) {
                  Text(
                     text = stringResource(id = R.string.room_name_hint),
                     color = Color.LightGray
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
            .height(200.dp),
         onColorSelected = {
            color = when(it) {
               Red().color -> Red()
               else -> Blue()
            }
         }
      )


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
         }
      ) {
         Spacer(modifier = Modifier.height(22.dp))
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
            targetValue = if (selected) c else Color.LightGray
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