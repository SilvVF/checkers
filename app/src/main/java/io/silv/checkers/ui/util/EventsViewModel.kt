package io.silv.checkers.ui.util

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class EventsViewModel<EVENT>: ViewModel() {

    protected val eventChannel = Channel<EVENT>()

    val events = eventChannel.receiveAsFlow()
}

@SuppressLint("ComposableNaming")
@Composable
public fun <EVENT : Any> EventsViewModel<EVENT>.collectEvents(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    state: (suspend (event: EVENT) -> Unit)
) {
    val stateFlow = events
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(stateFlow, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            stateFlow.collect { state(it) }
        }
    }
}