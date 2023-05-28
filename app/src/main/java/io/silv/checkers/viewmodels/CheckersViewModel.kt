package io.silv.checkers.viewmodels

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.errorprone.annotations.Immutable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.Blue
import io.silv.checkers.Board
import io.silv.checkers.Cord
import io.silv.checkers.Empty
import io.silv.checkers.Piece
import io.silv.checkers.Red
import io.silv.checkers.Room
import io.silv.checkers.firebase.boardStateFlow
import io.silv.checkers.firebase.deleteRoomCallbackFlow
import io.silv.checkers.firebase.roomStateFlow
import io.silv.checkers.firebase.updateBoardCallbackFlow
import io.silv.checkers.firebase.updateBoardNoMove
import io.silv.checkers.screens.Turn
import io.silv.checkers.toPieceList
import io.silv.checkers.ui.util.EventsViewModel
import io.silv.checkers.usecase.DeleteRoomUseCase
import io.silv.checkers.usecase.UpdateBoardUseCase
import io.silv.checkers.usecase.checkPieceForLoss
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheckersViewModel(
    savedStateHandle: SavedStateHandle,
    private val db: DatabaseReference,
    auth: FirebaseAuth,
    private val deleteRoomUseCase: DeleteRoomUseCase,
    private val updateBoardUseCase: UpdateBoardUseCase,
    val roomId: String = savedStateHandle["roomId"] ?: "",
): EventsViewModel<CheckersEvent>() {

    private val userId = auth.currentUser?.uid ?: "user"
    private var afterJumpEnd: Cord? = null

    private val room = db.roomStateFlow(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Room())

    private val board = db.boardStateFlow(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Board())

    val uiState = combine(
        room,
        board
    ) { room, board ->
        CheckerUiState(
            room = room,
            board = board.data.toPieceList(),
            turnPiece = if(board.turn == 1) Red() else Blue(),
            playerPiece = if(room.usersToColorChoice[userId] == 1) Red() else Blue(),
            turnMe = board.turn == room.usersToColorChoice[userId]
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), CheckerUiState())

    fun onDropAction(from: Cord, to: Cord, piece: Piece) = viewModelScope.launch {
        if(uiState.value.turnMe && piece.value == uiState.value.playerPiece.value) {
            if (afterJumpEnd != null && from != afterJumpEnd) {
                return@launch
            }
            val turnBeforeUpdate = board.value.turn
            updateBoardUseCase(board.value, from, to, roomId)
                .onFailure {
                    it.printStackTrace()
                    eventChannel.send(
                        CheckersEvent.MoveFailed(it.localizedMessage ?: "Error")
                    )
                }
                .onSuccess {
                    afterJumpEnd = if (it.turn == turnBeforeUpdate) { to } else { null }
                }
        }
    }

}

sealed interface CheckersEvent {
    data class MoveFailed(val reason: String): CheckersEvent
    data class FailedToDeleteRoom(val reason: String): CheckersEvent
}

@Immutable
@Stable
data class CheckerUiState(
    val room: Room = Room(),
    val board: List<List<Piece>> = emptyList(),
    val playerPiece: Piece = Empty,
    val turnMe: Boolean = false,
    val turnPiece: Piece = Empty,
    val timeToMove: Int = 0,
    val ableToForceOpponentMove: Boolean = false,
    val winner: Piece? = null
)