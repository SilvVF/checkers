package io.silv.checkers.navigation.logged_out

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class LoggedOutNavTarget: Parcelable {

    @Parcelize
    object PlayBot: LoggedOutNavTarget()

    @Parcelize
    object Auth: LoggedOutNavTarget()
}
