package io.silv.checkers

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import io.silv.checkers.usecase.generateInitialBoard
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.UUID

@Parcelize
@Immutable
@Stable
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

@Immutable
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

@Immutable
@Stable
@IgnoreExtraProperties
data class Board(
    val roomId: String = "",
    val turn: Int = listOf(1, 2).random(),
    val data: List<List<JsonPiece>> = generateInitialBoard().map { list -> list.map { it.toJsonPiece() } },
    val moves: List<Pair<Cord, Cord>> = emptyList()
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "roomId" to roomId,
            "data" to data,
            "moves" to moves,
            "turn" to turn
        )
    }
}

@Immutable
@Stable
@Parcelize
data class UiRoom(
    val id: String,
    val name: String,
    val moveTime: String,
    val dateCreated: String,
): Parcelable

typealias DropData = Pair<Cord, Piece>

typealias Cord = Pair<Int, Int>

@Immutable
@Stable
@Serializable
data class JsonPieceList(
    val list: List<List<JsonPiece>>
)

@Immutable
@Stable
@Serializable
data class JsonPiece(
    val value: Int = 0,
    val crowned: Boolean = false
)

fun Piece.toJsonPiece() = when(this) {
    is Red -> JsonPiece(1, crowned)
    is Blue -> JsonPiece(2, crowned)
    else -> JsonPiece(0, false)
}

@Immutable
@Stable
sealed class Piece(
    val value: Int = 0,
    val color: Color = Color.Transparent,
    open val crowned: Boolean = false,
)


@Immutable
@Stable
object Empty: Piece(0, Color.Transparent, false)

@Immutable
@Stable
data class Red(override val crowned: Boolean = false): Piece(1, Color.Red, crowned)

@Immutable
@Stable
data class Blue(override val crowned: Boolean = false): Piece(2, Color.Blue, crowned)