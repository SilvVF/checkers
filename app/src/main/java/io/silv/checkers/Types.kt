package io.silv.checkers

import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import io.silv.checkers.usecase.generateInitialBoard
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.UUID

@Parcelize
@IgnoreExtraProperties
data class Room(
    val id: String = UUID.randomUUID().toString(),
    val name: String ="",
    val usersToColorChoice: Map<String, Int> = mapOf(),
    val moveTimeSeconds: Int = 1,
    val createdAtEpochSecond: Long = 0L
) : Parcelable {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name ,
            "usersToColorChoice" to usersToColorChoice,
            "moveTimeSeconds" to moveTimeSeconds,
            "createdAtEpochSecond" to createdAtEpochSecond,
        )
    }
}

@Stable
@IgnoreExtraProperties
data class User(
    val id: String = "",
    val joinedRoomId: String = "",
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "joinedRoomId" to joinedRoomId
        )
    }
}

data class Move(
    val to: List<Int> = emptyList(),
    val from: List<Int> = emptyList()
)

enum class Turn(val value: Int) {
    Blue(2),
    Red(1),
    None(0)
}


@IgnoreExtraProperties
data class Board(
    val roomId: String = "",
    val turn: Int = listOf(1, 2).random(),
    val data: JsonPieceList = JsonPieceList(
        list = generateInitialBoard().map { list -> list.map { it.toJsonPiece() } }
    ),
    val moves: List<Move> = emptyList()
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "roomId" to roomId,
            "turn" to turn,
            "data" to data,
            "moves" to moves,
        )
    }
}

@Parcelize
data class UiRoom(
    val id: String,
    val name: String,
    val moveTime: String,
    val dateCreated: String,
): Parcelable

typealias DropData = Pair<Cord, Piece>

typealias Cord = Pair<Int, Int>


@Serializable
data class JsonPieceList(
    val list: List<List<JsonPiece>> = emptyList()
)

fun JsonPieceList.toPieceList() = this.list.map {
    it.map { piece ->
        when (piece.value) {
            Red().value -> Red(piece.crowned)
            Blue().value -> Blue(piece.crowned)
            else -> Empty
        }
    }
}
@Serializable
data class JsonPiece(
    val value: Int = 0,
    val crowned: Boolean = false
)

fun Piece.toJsonPiece() = when(this) {
    is Red -> JsonPiece(1, this.crowned)
    is Blue -> JsonPiece(2, this.crowned)
    else -> JsonPiece(0, false)
}


sealed class Piece(
    val value: Int = 0,
    val color: Color = Color.Transparent,
    open val crowned: Boolean = false,
)

fun Piece.getString(): String {
    return when(this) {
        is Red -> "Red"
        is Blue -> "Blue"
        else -> "Empty"
    }
}

object Empty: Piece(0, Color.Transparent, false)

data class Red(override val crowned: Boolean = false): Piece(1, Color.Red, crowned)

data class Blue(override val crowned: Boolean = false): Piece(2, Color.Blue, crowned)