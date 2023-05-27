package io.silv.checkers.navigation.game

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class GameNavTarget: Parcelable {

    @Parcelize
    data class Connecting(val roomId: String): GameNavTarget()

    @Parcelize
    data class Queue(val roomId: String): GameNavTarget()

    @Parcelize
    data class Game(val roomId: String): GameNavTarget()
}
