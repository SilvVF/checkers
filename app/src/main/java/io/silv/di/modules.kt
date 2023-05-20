package io.silv.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.silv.checkers.MainActivityViewModel
import io.silv.checkers.screens.CreateRoomViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    single<FirebaseAuth> { Firebase.auth }

    single<DatabaseReference> { Firebase.database.reference }

    viewModelOf(::CreateRoomViewModel)

    viewModelOf(::MainActivityViewModel)
}