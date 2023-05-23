package io.silv.checkers.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.silv.checkers.Blue
import io.silv.checkers.Empty
import io.silv.checkers.Piece
import io.silv.checkers.Red
import io.silv.checkers.ui.CheckerBoard
import io.silv.checkers.viewmodels.CheckerUiState

fun Turn.correctPieceForTurn(piece: Piece): Boolean {
    return when (this) {
        Turn.Red -> piece is Red
        Turn.Blue -> piece is Blue
    }
}

enum class Turn {
    Red, Blue
}


@Composable
fun CheckersScreen(
    state: CheckerUiState.Playing
) {
    

    BackHandler {
        // stop dragging from closing the app
    }
    
    val board by remember(state.board) {
        derivedStateOf { 
            state.board.data.map {list ->
                list.map { jsonPiece -> 
                    when (jsonPiece.value) {
                        Red().value -> Red(jsonPiece.crowned)
                        Blue().value -> Blue(jsonPiece.crowned)
                        else -> Empty
                    }
                }
            }
        }
    }
    
    val turn by remember(state.board) {
        derivedStateOf { 
            when(state.board.turn) {
                Red().value -> Turn.Red
                else -> Turn.Blue
            }
        }
    }
    
    CheckerBoard(
        board = board,
        turn = turn,
        onDropAction = { fromCord, toCord, piece ->  
            
        } 
    )
    

}