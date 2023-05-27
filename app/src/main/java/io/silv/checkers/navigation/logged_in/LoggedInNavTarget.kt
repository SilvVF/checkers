package io.silv.checkers.navigation.logged_in

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class LoggedInNavTarget: Parcelable {

    @Parcelize
    object SearchRooms: LoggedInNavTarget()

    @Parcelize
    object CreateRoom: LoggedInNavTarget()

    @Parcelize
    data class CheckersGame(val roomId: String): LoggedInNavTarget()

    @Parcelize
    object PlayBot: LoggedInNavTarget()
}