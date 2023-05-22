package io.silv.checkers.usecase

import io.silv.checkers.Room
import io.silv.checkers.UiRoom
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun Room.toUiRoom(): UiRoom {
    return UiRoom(
        id = id,
        name = name,
        moveTime = formatTime(moveTime),
        dateCreated = Instant.ofEpochSecond(createdAt)
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