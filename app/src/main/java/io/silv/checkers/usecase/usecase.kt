package io.silv.checkers.usecase

import io.silv.checkers.Empty
import io.silv.checkers.Room
import io.silv.checkers.UiRoom
import io.silv.checkers.ui.dragdrop.getPiece
import io.silv.checkers.ui.dragdrop.isEven
import io.silv.checkers.ui.dragdrop.isOdd
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun Room.toUiRoom(): UiRoom {
    return UiRoom(
        id = id,
        name = name,
        moveTime = formatTime(moveTimeSeconds),
        dateCreated = Instant.ofEpochSecond(createdAtEpochSecond)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
    )
}

fun formatTime(it: Int): String =
    if (it <= 60) {
        "$it seconds"
    } else {
        var temp = it
        var minutes = 0
        while (temp >= 60) {
            temp -= 60
            minutes += 1
        }
        "$minutes minutes $temp seconds"
    }

fun generateInitialBoard() = List(8) { i ->
    val startWithPiece = i.isOdd()
    val piece = getPiece(i)
    List(8) { j ->
        when {
            startWithPiece && j.isEven() -> piece
            !startWithPiece && j.isOdd() -> piece
            else ->  Empty
        }
    }
}
