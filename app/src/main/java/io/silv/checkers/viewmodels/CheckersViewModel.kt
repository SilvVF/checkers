package io.silv.checkers.viewmodels

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import io.silv.checkers.usecase.checkPieceForLoss
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheckersViewModel(
    db: DatabaseReference,
    auth: FirebaseAuth,
    private val deleteRoomUseCase: DeleteRoomUseCase,
    private val updateBoardUseCase: UpdateBoardUseCase,
    private val updateBoardNoMoveUseCase: UpdateBoardNoMoveUseCase,
    val roomId: String,
): EventsViewModel<CheckersEvent>() {

    private val userId = auth.currentUser?.uid ?: auth.currentUser?.providerId
    var afterJumpEnd: Cord? by mutableStateOf(null)
            private set

    private val room = db.roomStateFlow(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Room())

    private val board = db.boardStateFlow(roomId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Board())


    private val turnToSecondsOnTurn = channelFlow {
        var turn: Int? = null
        var turnStartTime = System.currentTimeMillis()
        board.collectLatest { board ->
            if (turn != board.turn) {
                turn = board.turn
                turnStartTime = System.currentTimeMillis()
            }
            while (true) {
                val timeOnTurnSeconds = ((System.currentTimeMillis() - turnStartTime) / 1000).toInt()
                send((turn ?: 0) to timeOnTurnSeconds)
                delay(50)
            }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0 to 0)

    val uiState = combine(
        room,
        board,
        turnToSecondsOnTurn
    ) { room, board, (turn, seconds) ->
        val turnMe = board.turn == room.usersToColorChoice[userId]
        val timeToMove = room.moveTimeSeconds - seconds
        val boardData =  board.data.toPieceList()
        CheckerUiState(
            room = room,
            board = boardData,
            turnPiece = if(board.turn == 1) Red() else Blue(),
            playerPiece = if(room.usersToColorChoice[userId] == 1) Red() else Blue(),
            turnMe = turnMe,
            timeToMove = timeToMove,
            turnsMatch = turn == board.turn,
            winner = when {
                turnMe && timeToMove < -10 -> if(room.usersToColorChoice[userId] == 1) Blue() else Red()
                timeToMove < -10 -> if(room.usersToColorChoice[userId] == 1) Red() else Blue()
                checkPieceForLoss(boardData, Red()) -> Blue()
                checkPieceForLoss(boardData, Blue()) -> Red()
                else -> null
            }
        )
    }
        .onEach { state ->
            startAutoMove(state, board.value)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), CheckerUiState())

    private var moveInProgress: Boolean = false

    private fun startAutoMove(state: CheckerUiState, board: Board) = viewModelScope.launch {
        if (state.turnMe && !moveInProgress && state.timeToMove == 0 && state.turnsMatch) {
            moveInProgress = true
            retryOnFailure(3) {
                updateBoardNoMoveUseCase(board, state.room.id)
                    .onFailure {
                        eventChannel.send(
                            CheckersEvent.MoveFailed(it.localizedMessage ?: "Error")
                        )
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

    fun endTurn() = viewModelScope.launch {
        if (!moveInProgress) {
            moveInProgress = true
            afterJumpEnd = null
            updateBoardNoMoveUseCase(board.value, roomId)
                .onFailure {
                    eventChannel.send(
                        CheckersEvent.MoveFailed(it.localizedMessage ?: "Error")
                    )
                }
        }
    }.invokeOnCompletion { moveInProgress = false }

    fun deleteRoom() = CoroutineScope(Dispatchers.IO).launch {
        retryOnFailure(3) {
            deleteRoomUseCase(roomId)
        }
    }

}

suspend fun <T> retryOnFailure(count: Int = 1, action: suspend () -> Result<T>) {
    var i = 1
    while (i <= count) {
        when(action().getOrNull()) {
            null -> i += 1
            else -> break
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
    val turnsMatch: Boolean = false,
    val winner: Piece? = null
)