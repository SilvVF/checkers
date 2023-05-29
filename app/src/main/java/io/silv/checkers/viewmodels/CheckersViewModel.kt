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
import io.silv.checkers.firebase.roomStateFlow
import io.silv.checkers.toPieceList
import io.silv.checkers.ui.util.EventsViewModel
import io.silv.checkers.usecase.DeleteRoomUseCase
import io.silv.checkers.usecase.UpdateBoardNoMoveUseCase
import io.silv.checkers.usecase.UpdateBoardUseCase
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheckersViewModel(
    savedStateHandle: SavedStateHandle,
    private val db: DatabaseReference,
    auth: FirebaseAuth,
    private val deleteRoomUseCase: DeleteRoomUseCase,
    private val updateBoardUseCase: UpdateBoardUseCase,
    private val updateBoardNoMoveUseCase: UpdateBoardNoMoveUseCase,
    val roomId: String = savedStateHandle["roomId"] ?: "",
): EventsViewModel<CheckersEvent>() {

    private val userId = auth.currentUser?.uid ?: "user"
    private var afterJumpEnd: Cord? = null

    private val room = db.roomStateFlow(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Room())

    private val board = db.boardStateFlow(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Board())


    private val secondsOnTurn = channelFlow {
        var turn: Int? = null
        var turnStartTime = System.currentTimeMillis()
        board.collectLatest { board ->
            if (turn != board.turn) {
                turn = board.turn
                turnStartTime = System.currentTimeMillis()
            }
            while (true) {
                val timeOnTurnSeconds = ((System.currentTimeMillis() - turnStartTime) / 1000).toInt()
                Log.d("secondsOnTurn", "boardTurn ${board.turn}  turn $turn  timeIn Seconds$timeOnTurnSeconds")
                send((turn ?: 0) to timeOnTurnSeconds)
                delay(50)
            }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0 to 0)

    val uiState = combine(
        room,
        board,
        secondsOnTurn
    ) { room, board, (turn, seconds) ->
        CheckerUiState(
            room = room,
            board = board.data.toPieceList(),
            turnPiece = if(board.turn == 1) Red() else Blue(),
            playerPiece = if(room.usersToColorChoice[userId] == 1) Red() else Blue(),
            turnMe = board.turn == room.usersToColorChoice[userId],
            timeToMove = room.moveTimeSeconds - seconds,
            turnsMatch = turn == board.turn
        )
    }
        .onEach { state ->
            startAutoMove(state, board.value)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), CheckerUiState())

    private var moveInProgress: Boolean = false

    private fun startAutoMove(state: CheckerUiState, board: Board, retry: Int = 0): DisposableHandle = viewModelScope.launch {
        if (state.turnMe && !moveInProgress && state.timeToMove == 0 && state.turnsMatch) {
            moveInProgress = true
            updateBoardNoMoveUseCase(board, state.room.id)
                .onFailure {
                    eventChannel.send(
                        CheckersEvent.MoveFailed(it.localizedMessage ?: "Error")
                    )
                    if (retry <= 2) {
                        startAutoMove(state, board, retry + 1)
                    }
                }
        }
    }.invokeOnCompletion { moveInProgress = false }

    fun onDropAction(from: Cord, to: Cord, piece: Piece) = viewModelScope.launch {
        if(uiState.value.turnMe && piece.value == uiState.value.playerPiece.value && !moveInProgress) {
            moveInProgress = true
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
    }.invokeOnCompletion { moveInProgress = false }

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
    val turnsMatch: Boolean = false,
    val winner: Piece? = null
)