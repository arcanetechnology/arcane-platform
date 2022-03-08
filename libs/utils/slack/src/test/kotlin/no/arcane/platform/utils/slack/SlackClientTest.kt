package no.arcane.platform.utils.slack

import io.kotest.core.spec.style.StringSpec

class SlackClientTest : StringSpec({

    "send message to slack channel".config(enabled = false) {
        SlackClient.sendMessage(
            channel = ChannelId(System.getenv("SLACK_CHANNEL_ID")),
            message = "Testing"
        )
    }
})