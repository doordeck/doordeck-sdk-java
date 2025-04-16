package com.doordeck.sdk.common.events

import com.doordeck.sdk.common.models.DoordeckEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow


/**
 * Events Manager : responsible for sending the event for those who subscribe to them,
 */
internal object EventsManager {
    private val _eventFlow = MutableSharedFlow<DoordeckEvent>(replay = 1)

    fun send(event: DoordeckEvent) {
        _eventFlow.tryEmit(event)
    }

    internal fun eventsFlow(): SharedFlow<DoordeckEvent> {
        return _eventFlow.asSharedFlow()
    }
}
