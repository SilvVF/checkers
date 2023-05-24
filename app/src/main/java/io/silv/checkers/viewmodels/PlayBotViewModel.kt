package io.silv.checkers.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.silv.checkers.Blue
import io.silv.checkers.Cord
import io.silv.checkers.Piece
import io.silv.checkers.Red
import io.silv.checkers.usecase.aiMove
import io.silv.checkers.usecase.crownPieces
import io.silv.checkers.usecase.evaluate
import io.silv.checkers.usecase.generateInitialBoard
import io.silv.checkers.usecase.getAllBoards
import io.silv.checkers.usecase.validMoves
import io.silv.checkers.usecase.validatePlacement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayBotViewModel: ViewModel() {

    val playerTurn = MutableStateFlow(true)

    val board = MutableStateFlow(generateInitialBoard())

    init {
        viewModelScope.launch {
            playerTurn.collect {
                if (!it) {
                    val data = withContext(Dispatchers.IO)  {
                        aiMove(board.value).second.crownPieces()
                    }
                    board.emit(data)
                    playerTurn.emit(true)
                }
            }
        }
    }

    fun onDropAction(
        b: List<List<Piece>>,
        from: Cord,
        to: Cord,
        piece: Piece
    ) = viewModelScope.launch {
        if (!playerTurn.value || piece.value == Red().value) { return@launch }
        val (valid, newBoard) = validatePlacement(b, from, to)
        if (valid) {
            board.emit(newBoard)
            playerTurn.emit(false)
        }
    }
}