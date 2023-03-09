package com.k33.platform.cms

import com.k33.platform.cms.events.SlackNotification
import io.kotest.core.spec.style.StringSpec

class SlackNotificationTest : StringSpec({

    "send slack notification on publish".config(enabled = false) {
        SlackNotification.notifySlack(
            pageId = ""
        )
    }
})