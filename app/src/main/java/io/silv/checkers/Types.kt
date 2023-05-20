package io.silv.checkers

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.google.firebase.database.DatabaseReference
import io.silv.checkers.firebase.Fb
import io.silv.checkers.ui.dragdrop.generateInitialBoard
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class Room(
    val id: String,
    val name: String,
    val turn: Int = 1,
    val users: List<String>,
    val board: List<List<Piece>> = generateInitialBoard()
)

fun Room.pushToDb(db: DatabaseReference) {
    db.child(Fb.roomsKey)
        .child(id).apply {
            this.child(Fb.boardKey).setValue(
                board.encodeToJsonPieceList()
            )
            this.child(Fb.turnKey).setValue(turn)
            this.child(Fb.nameKey).setValue(name)
        }
        .child(Fb.usersKey)
        .child(Fb.userIdKey)
        .apply {
            users.forEach { uid ->
                this.child(uid).setValue(true)
            }
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

fun JsonPiece.toPiece() = when(value) {
    1 -> Red(crowned)
    2 -> Blue(crowned)
    else -> Empty
}

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

fun List<List<Piece>>.encodeToJsonPieceList() = Json.encodeToString(
    JsonPieceList.serializer(),
    JsonPieceList(
        this.map { it.map { p -> p.toJsonPiece() } }
    )
)


object Empty: Piece(0, Color.Transparent, false)

data class Red(override val crowned: Boolean = false): Piece(1, Color.Red, crowned)

data class Blue(override val crowned: Boolean = false): Piece(2, Color.Blue, crowned)