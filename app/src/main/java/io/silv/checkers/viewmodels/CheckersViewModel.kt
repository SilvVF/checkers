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
import io.silv.checkers.ui.util.EventsViewModel
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
import kotlinx.serialization.json.Json

class CheckersViewModel(
    savedStateHandle: SavedStateHandle,
    private val db: DatabaseReference,
    private val auth: FirebaseAuth,
    val roomId: String = savedStateHandle["roomId"] ?: "",
): EventsViewModel<CheckersEvent>() {

    private val userId = auth.currentUser?.uid ?: "user"
    private var autoMoveJob: Job? = null
    private var lastMoveEnd: Cord? = null
    private val moveInProgress = MutableStateFlow(false)
    private val ableToForceOpponentMove = MutableStateFlow(false)

    private val room = flow {
        db.roomStateFlow(roomId).collect { room ->
            emit(room)
        }
    }
        .flowOn(Dispatchers.IO)

    data class BoardTime(
        val seconds: Int,
        val board: Board
    )

    private val boardTime = channelFlow {
        var seconds = 0
        var prevBoard: Board? = null
        db.boardStateFlow(roomId).collectLatest {
            while(true) {
                if (prevBoard?.turn != it.turn) {
                    lastMoveEnd = null
                    ableToForceOpponentMove.emit(false)
                    seconds = 0
                }
                delay(1000)
                send(BoardTime(seconds, it))
                seconds += 1
                prevBoard = it
            }
        }
        awaitClose()
    }
        .flowOn(Dispatchers.IO)



    val uiState = combine(
        room,
        boardTime
    ) { room, boardTime ->
        createCheckerUiState(boardTime, room)
    }
        .onEach {
            if (it.timeToMove == 0 && it.turnMe) {
                autoMoveJob = viewModelScope.launch {
                    moveInProgress.emit(true)
                    db.updateBoardNoMove(it.rawBoard, roomId)
                        .first()
                    moveInProgress.emit(false)
                }
            }
            if (it.timeToMove == 0 && !it.turnMe) {
                viewModelScope.launch {
                    delay(5000)
                    ableToForceOpponentMove.emit(true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CheckerUiState())


    private fun createCheckerUiState(boardTime: BoardTime, room: Room) = CheckerUiState(
            room = room,
            board = boardTime.board.data.list.map { list ->
                list.map { jsonPiece ->
                    when(jsonPiece.value) {
                        Blue().value -> Blue(jsonPiece.crowned)
                        Red().value -> Red(jsonPiece.crowned)
                        else -> Empty
                    }
                }
            },
            turnMe = room.usersToColorChoice[userId] == boardTime.board.turn,
            turn = when(boardTime.board.turn) {
                Blue().value -> Turn.Blue
                else -> Turn.Red
            },
            playerPiece = when(room.usersToColorChoice[userId]) {
                Blue().value -> Blue()
                else -> Red()
            },
            timeToMove = (room.moveTimeSeconds - boardTime.seconds).coerceAtLeast(0)
    )

    fun deleteRoom(roomId: String) = viewModelScope.launch {
        db.deleteRoomCallbackFlow(roomId)
            .catch {
                it.printStackTrace()
            }
            .first()
    }

    fun onDropAction(
        room: Room,
        board: Board,
        data: List<List<Piece>>,
        from: Cord,
        to: Cord,
        piece: Piece
    ) = viewModelScope.launch {
        Log.d("ONDROP", "$lastMoveEnd")
        if (uiState.value.turnMe && uiState.value.timeToMove > 0 && !moveInProgress.value) {
            moveInProgress.emit(true)
            if (lastMoveEnd != from && lastMoveEnd != null) {
                Log.d("ONDROP", "JUMP ERROR")
                eventChannel.send(
                    CheckersEvent.MoveFailed("You can only make extra jumps in the same turn")
                )
            }
            val result = db.updateBoardCallbackFlow(board, data, from, to, room.id, piece)
                .catch {
                    Log.d("ONDROP", it.localizedMessage ?: "unknown")
                    eventChannel.send(
                        CheckersEvent.MoveFailed(it.localizedMessage ?: "Unknown Errors")
                    )
                }
                .first()
            Log.d("ONDROP", "result $result")
            if (result) {
                lastMoveEnd = to
            }
        } else {
            Log.d("ONDROP", "INVALID MOVE NOT MY TURN ERROR")
            eventChannel.send(
                CheckersEvent.MoveFailed("Invalid move")
            )
        }
        moveInProgress.emit(false)
    }

}

sealed interface CheckersEvent {
    data class MoveFailed(val reason: String): CheckersEvent
}

@Immutable
@Stable
data class CheckerUiState(
    val room: Room = Room(),
    val rawBoard: Board = Board(),
    val board: List<List<Piece>> = emptyList(),
    val turn: Turn = Turn.Blue,
    val turnMe: Boolean = false,
    val timeToMove: Int = 0,
    val playerPiece: Piece = Empty,
)