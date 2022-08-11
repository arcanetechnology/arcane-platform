package no.arcane.platform.utils.slack

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SlackClientTest : StringSpec({

    "send message to slack channel".config(enabled = false) {
        SlackClient.sendMessage(
            channel = ChannelId(System.getenv("SLACK_CHANNEL_ID")),
            message = "Testing"
        )
    }

    "get private channel id".config(enabled = false) {
        SlackClient.getChannelId("") shouldBe ""
    }

    "get public channel id".config(enabled = false) {
        SlackClient.getChannelId("") shouldBe ""
    }

    "get all channel ids".config(enabled = false) {
        SlackClient.getChannelNameToIdMap().forEach { (key, value) ->
            println("$key => $value")
        }
    }
})