package com.k33.platform.app.invest

import com.k33.platform.utils.slack.ChannelId
import com.k33.platform.utils.slack.ChannelName
import com.k33.platform.utils.slack.SlackClient
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object SlackNotification {

    private val slackProInvestorChannel by lazy {
        System.getenv("SLACK_PROFESSIONAL_INVESTORS_CHANNEL_ID")?.let { ChannelId(it) }
            ?: System.getenv("SLACK_PROFESSIONAL_INVESTORS_CHANNEL_NAME")?.let { ChannelName(it) }
            ?: ChannelName("professional-investors")
    }

    private val slackInvestChannel by lazy {
        System.getenv("SLACK_INVEST_CHANNEL_ID")?.let { ChannelId(it) }
            ?: System.getenv("SLACK_INVEST_CHANNEL_NAME")?.let { ChannelName(it) }
            ?: ChannelName("invest")
    }

    suspend fun notifySlack(
        strFundInfoRequest: String,
        testMode: Boolean,
    ) {
        val channel = if (testMode) slackInvestChannel else slackProInvestorChannel
        SlackClient.sendRichMessage(
            channel,
            strFundInfoRequest,
        ) {
            header {
                text("K33 Assets Fund Inquiry")
            }
            section {
                markdownText("```$strFundInfoRequest```")
            }
            divider()
            context {
                elements {
                    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                    markdownText("Timestamp: ${ZonedDateTime.now(ZoneOffset.UTC).format(formatter)} (UTC)")
                }
            }
        }
    }
}