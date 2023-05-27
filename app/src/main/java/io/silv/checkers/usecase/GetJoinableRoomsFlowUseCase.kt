package io.silv.checkers.usecase

import com.google.firebase.database.DatabaseReference
import io.silv.checkers.UiRoom
import io.silv.checkers.firebase.roomsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetJoinableRoomsFlowUseCase(
    private val db: DatabaseReference
) {

    operator fun invoke(

    ): Flow<List<UiRoom>> {
        return db.roomsFlow()
            .map {
                it.filter { room ->
                    room.usersToColorChoice.values.size < 2
                }.map { room ->
                    room.toUiRoom()
                }.sortedByDescending { uiRoom ->
                    uiRoom.dateCreated
                }
            }
            .flowOn(Dispatchers.IO)
    }
}