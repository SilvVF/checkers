package io.silv.checkers.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.silv.checkers.Blue
import io.silv.checkers.Cord
import io.silv.checkers.Piece
import io.silv.checkers.Red
import io.silv.checkers.usecase.aiMove
import io.silv.checkers.usecase.checkBoardForWinner
import io.silv.checkers.usecase.crownPieces
import io.silv.checkers.usecase.generateInitialBoard
import io.silv.checkers.usecase.makeRandomMove
import io.silv.checkers.usecase.moreJumpsPossible
import io.silv.checkers.usecase.validatePlacement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayBotViewModel: ViewModel() {

    private val playerTurn = MutableStateFlow(true)

    val board = MutableStateFlow(generateInitialBoard())

    init {
        viewModelScope.launch {
            playerTurn.collect {
                if (!it) {
                    var data = withContext(Dispatchers.IO)  {
                        aiMove(board.value).second
                    }
                    if (data == board.value) {
                        data = makeRandomMove(board.value)
                    }
                    board.emit(data.crownPieces())
                    if (checkBoardForWinner(data, Red())) {
                        board.emit(generateInitialBoard())
                        playerTurn.emit(true)
                    } else {
                        playerTurn.emit(true)
                    }
                } else {
                    lastMove = null
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
        val (valid,removed, newBoard) = validatePlacement(b, from, to)
        if (valid) {
            lastMove = to
            board.emit(newBoard)
            if (checkBoardForWinner(newBoard, Blue())) {
                Log.d("CHECK", "TRUE")
                board.emit(generateInitialBoard())
                playerTurn.emit(true)
            } else {
                playerTurn.emit(
                    if (removed != null) {
                        moreJumpsPossible(newBoard, to, Blue())
                    } else {
                        false
                    }
                )
            }
        }
    }
}