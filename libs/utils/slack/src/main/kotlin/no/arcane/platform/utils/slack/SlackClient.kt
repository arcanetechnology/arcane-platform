package no.arcane.platform.utils.slack

import com.slack.api.RequestConfigurator
import com.slack.api.Slack
import com.slack.api.SlackConfig
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.model.ConversationType
import com.slack.api.model.kotlin_extension.block.dsl.LayoutBlockDsl
import com.slack.api.model.kotlin_extension.block.withBlocks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import no.arcane.platform.utils.logging.getLogger

object SlackClient {

    private val logger by getLogger()

    private val asyncMethodsClient by lazy {
        val slackConfig = SlackConfig()
        slackConfig.isPrettyResponseLoggingEnabled = true
        Slack
            .getInstance(slackConfig)
            .methodsAsync(System.getenv("SLACK_TOKEN"))
    }

    suspend fun sendMessage(
        channel: Channel,
        message: String,
    ) {
        val channelId = channel.getId()
        chatPostMessage { req ->
            req
                .channel(channelId)
                .unfurlLinks(true)
                .text(message)
        }
    }

    suspend fun sendRichMessage(
        channel: Channel,
        altPlainTextMessage: String,
        builder: LayoutBlockDsl.() -> Unit,
    ) {
        val channelId = channel.getId()
        chatPostMessage { req ->
            req
                .channel(channelId)
                .unfurlLinks(true)
                .text(altPlainTextMessage)
                .blocks(withBlocks(builder))
        }
    }

    private suspend fun chatPostMessage(
        request: RequestConfigurator<ChatPostMessageRequest.ChatPostMessageRequestBuilder>
    ) {
        try {
            val response = withContext(Dispatchers.IO) {
                asyncMethodsClient.chatPostMessage(request)
            }.await()
            if (response.isOk) {
                logger.info(response.message.text)
            } else {
                response.warning?.let { logger.warn(it) }
                response.error?.let { logger.error(it) }
                response.errors?.let { logger.error(it.toString()) }
            }
        } catch (e: Exception) {
            logger.error("Failed to send message to slack", e)
        }
    }

    suspend fun getChannelId(channelName: String): String? = getChannelNameToIdMap()[channelName]

    internal tailrec suspend fun getChannelNameToIdMap(
        acc: Map<String, String> = emptyMap(),
        cursor: String? = null
    ): Map<String, String> {
        if (cursor == "") {
            return acc
        }
        val response = asyncMethodsClient.conversationsList { req ->
            req.types(
                mutableListOf(
                    ConversationType.PUBLIC_CHANNEL,
                    ConversationType.PRIVATE_CHANNEL,
                )
            )
            req.cursor(cursor)
        }.await()
        if (!response.isOk) {
            response.warning?.let { logger.warn(it) }
            response.error?.let { logger.error(it) }
            return acc
        }
        val map = response.channels.associate { it.name to it.id }
        map.forEach { (key, value) ->
            logger.info("$key = $value")
        }
        val nextCursor = response.responseMetadata.nextCursor
        return getChannelNameToIdMap(acc + map, nextCursor)
    }
}