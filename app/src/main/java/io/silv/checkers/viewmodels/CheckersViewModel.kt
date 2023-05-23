package io.silv.checkers.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.Board
import io.silv.checkers.Cord
import io.silv.checkers.Piece
import io.silv.checkers.Room
import io.silv.checkers.firebase.boardStateFlow
import io.silv.checkers.firebase.roomStateFlow
import io.silv.checkers.firebase.updateBoardCallbackFlow
import io.silv.checkers.firebase.updateBoardNoMove
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheckersViewModel(
    savedStateHandle: SavedStateHandle,
    private val db: DatabaseReference,
    private val auth: FirebaseAuth,
    val roomId: String? = savedStateHandle["roomId"],
): ViewModel() {



    private val connectingTime = flow {
        var seconds = 0
        while(true) {
            delay(1000)
            seconds += 1
            emit(seconds)
        }
    }
        .flowOn(Dispatchers.IO)


    private val room = flow {
        roomId?.let {
            db.roomStateFlow(it).collect { room ->
                emit(room)
            }
        }
    }
        .flowOn(Dispatchers.IO)


    private val board = flow {
        room.collect {
            db.boardStateFlow(it.id).collect {
                emit(it)
            }
        }
    }
        .flowOn(Dispatchers.IO)



    private val playerCount = room.map { room ->
        room.usersToColorChoice.size
    }
        .flowOn(Dispatchers.Default)

    val uiState = combine(
        room,
        playerCount,
        connectingTime,
        board
    ) { room,  playerCount, connectingTime, board ->
        if (playerCount == 2) {
            CheckerUiState.Playing(room, board)
        } else {
            CheckerUiState.Queue(room)
        }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            CheckerUiState.Connecting
        )

    private var autoMoveJob: Job? = null

    init {
        viewModelScope.launch {
            board.collectLatest { board ->
                when (val state = uiState.first()) {
                    is CheckerUiState.Playing -> {
                        val userTurn = state.room.usersToColorChoice[auth.currentUser?.uid]
                            ?: return@collectLatest
                        if (board.turn == userTurn) {
                            autoMoveJob = CoroutineScope(Dispatchers.IO).launch {
                                delay(state.room.moveTimeSeconds.toLong())
                                db.updateBoardNoMove(board, state.room.id)
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }


    fun onDropAction(
        room: Room,
        board: Board,
        from: Cord,
        to: Cord,
        piece: Piece
    ) = viewModelScope.launch {
        if (room.usersToColorChoice[auth.currentUser?.uid ?: return@launch] == board.turn) {
            val result = db.updateBoardCallbackFlow(board, from, to, room.id).first()
            if (result) {
                autoMoveJob?.cancel()
            } else {
                // TODO("Retry making move or have other user auto win to disconnect")
            }
        }
    }

}

sealed interface CheckerUiState {
    object Connecting: CheckerUiState
    data class Queue(val room: Room): CheckerUiState
    data class Playing(
        val room: Room,
        val board: Board,
    ): CheckerUiState
}