package no.arcane.platform.cms

import io.kotest.core.spec.style.StringSpec
import no.arcane.platform.cms.events.SlackNotification

class SlackNotificationTest : StringSpec({

    "send slack notification on publish".config(enabled = false) {
        SlackNotification.notifySlack(
            pageId = ""
        )
    }
})