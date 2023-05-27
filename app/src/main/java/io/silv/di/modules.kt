package io.silv.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.silv.checkers.usecase.ConnectToRoomUseCase
import io.silv.checkers.usecase.CreateRoomUseCase
import io.silv.checkers.usecase.DeleteRoomUseCase
import io.silv.checkers.usecase.GetJoinableRoomsFlowUseCase
import io.silv.checkers.viewmodels.CheckersViewModel
import io.silv.checkers.viewmodels.CreateRoomViewModel
import io.silv.checkers.viewmodels.MainActivityViewModel
import io.silv.checkers.viewmodels.PlayBotViewModel
import io.silv.checkers.viewmodels.SearchRoomViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule = module {

    single<FirebaseAuth> { Firebase.auth }

    single<DatabaseReference> { Firebase.database.reference }

    factoryOf(::GetJoinableRoomsFlowUseCase)

    factoryOf(::ConnectToRoomUseCase)

    factoryOf(::DeleteRoomUseCase)

    factoryOf(::CreateRoomUseCase)

    viewModelOf(::CreateRoomViewModel)

    viewModelOf(::MainActivityViewModel)

    viewModelOf(::SearchRoomViewModel)

    viewModel { parameters ->
        CheckersViewModel(
            savedStateHandle = get(),
            db = get(),
            auth = get(),
            deleteRoomUseCase = get(),
            roomId = parameters.get()
        )
    }

    viewModelOf(::PlayBotViewModel)

}