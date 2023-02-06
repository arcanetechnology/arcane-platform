package com.k33.platform.cms.events

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.k33.platform.utils.logging.getLogger

typealias Callback = suspend (EventType, String) -> Unit

object EventHub {

    private val logger by getLogger()
    private val listeners = mutableMapOf<EventPattern, MutableList<Callback>>()

    fun subscribe(
        eventPattern: EventPattern,
        callback: Callback
    ) {
        listeners.getOrPut(eventPattern) { mutableListOf() } += callback
    }

    suspend fun notify(
        eventType: EventType,
        id: String
    ) {
        coroutineScope {
            setOf(
                EventPattern(),
                EventPattern(eventType.resource),
                EventPattern(action = eventType.action),
                EventPattern(eventType.resource, eventType.action)
            )
                .flatMap { eventType -> listeners[eventType] ?: emptySet() }
                .forEach { callback ->
                    launch {
                        try {
                            callback(eventType, id)
                        } catch (e: Exception) {
                            logger.error("Exception in Eventhub callback", e)
                        }
                    }
                }
        }
    }
}