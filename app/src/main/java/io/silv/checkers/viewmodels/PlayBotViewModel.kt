package io.silv.checkers.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.silv.checkers.Blue
import io.silv.checkers.Cord
import io.silv.checkers.Piece
import io.silv.checkers.Red
import io.silv.checkers.usecase.AiOpponent
import io.silv.checkers.usecase.checkPieceForLoss
import io.silv.checkers.usecase.generateInitialBoard
import io.silv.checkers.usecase.moreJumpsPossible
import io.silv.checkers.usecase.validatePlacement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch

class PlayBotViewModel: ViewModel() {

    private val playerTurn = MutableStateFlow(true)
    private val _extraJumpsAvailable = MutableStateFlow(false)
    val extraJumpsAvailable = _extraJumpsAvailable.asStateFlow()
    val board = MutableStateFlow(generateInitialBoard())
    private val aiOpponent = AiOpponent()

    init {
        viewModelScope.launch {
            playerTurn.collect { isPlayerTurn ->
                if (isPlayerTurn) {
                    lastMove = null
                } else {
                    val boardAfterAiMove = aiOpponent.makeMove(board.value)
                    board.emit(boardAfterAiMove)
                    if (checkPieceForLoss(boardAfterAiMove, Red())) {
                        resetGame()
                    }
                    playerTurn.emit(true)
                }
            }
        }
    }

    private var lastMove: Cord? = null

    fun onDropAction(
        b: List<List<Piece>>,
        from: Cord,
        to: Cord,
        piece: Piece
    ) = viewModelScope.launch {
        if (!playerTurn.value || piece.value == Red().value || (lastMove != null && from != lastMove)) { return@launch }
        val (valid, removed, newBoard) = validatePlacement(b, from, to)
        if (valid) {
            lastMove = to
            board.emit(newBoard)
            if (checkPieceForLoss(newBoard, Blue())) {
                Log.d("CHECK", "TRUE")
                resetGame()
            } else {
               if(removed != null && moreJumpsPossible(newBoard, lastMove ?: (0 to 0))) {
                   _extraJumpsAvailable.emit(true)
               } else {
                   _extraJumpsAvailable.emit(false)
                   playerTurn.emit(false)
               }
            }
        }
    }

    fun changeTurn() = viewModelScope.launch {
        playerTurn.getAndUpdate { !it }
        _extraJumpsAvailable.emit(false)
    }

    fun resetGame() = viewModelScope.launch {
        board.emit(generateInitialBoard())
        lastMove = null
        playerTurn.emit(true)
        _extraJumpsAvailable.emit(false)
    }
}