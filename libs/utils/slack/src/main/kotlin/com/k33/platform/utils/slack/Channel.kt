package com.k33.platform.utils.slack

import kotlinx.coroutines.coroutineScope

sealed class Channel {
    abstract suspend fun getId(): String?
}

data class ChannelName(private val name: String) : Channel() {
    override suspend fun getId() = coroutineScope {
        SlackClient.getChannelId(name)
    }
}

data class ChannelId(private val id: String) : Channel() {
    override suspend fun getId() = id
}
