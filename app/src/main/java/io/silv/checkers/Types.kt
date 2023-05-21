package io.silv.checkers

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import io.silv.checkers.ui.dragdrop.generateInitialBoard
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@IgnoreExtraProperties
data class Room(
    val id: String = UUID.randomUUID().toString(),
    val name: String ="",
    val users: Map<String, Int> = mapOf(),
    val moveTime: Int = 1,
    val createdAt: Long = 0L
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name ,
            "users" to users,
            "moveTime" to moveTime,
            "createdAt" to createdAt,
        )
    }
}

@IgnoreExtraProperties
data class Board(
    val roomId: String,
    val turn: Int = listOf(1, 2).random(),
    val data: JsonPieceList = JsonPieceList(
        list = generateInitialBoard().map {
            it.map { p -> p.toJsonPiece() }
        }
    ),
    val moves: Map<String, Pair<Cord, Cord>> = emptyMap()
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "roomId" to roomId,
            "data" to Json.encodeToString(data),
            "moves" to moves,
            "turn" to turn
        )
    }
}


typealias DropData = Pair<Cord, Piece>

typealias Cord = Pair<Int, Int>

@Serializable
data class JsonPieceList(
    val list: List<List<JsonPiece>>
)

@Serializable
data class JsonPiece(
    val value: Int,
    val crowned: Boolean
)

fun Piece.toJsonPiece() = when(this) {
    is Red -> JsonPiece(1, crowned)
    is Blue -> JsonPiece(2, crowned)
    else -> JsonPiece(0, false)
}

@Immutable
sealed class Piece(
    val value: Int,
    val color: Color,
    open val crowned: Boolean,
)



object Empty: Piece(0, Color.Transparent, false)

data class Red(override val crowned: Boolean = false): Piece(1, Color.Red, crowned)

data class Blue(override val crowned: Boolean = false): Piece(2, Color.Blue, crowned)