package com.k33.platform.cms

import io.kotest.core.spec.style.StringSpec
import com.k33.platform.cms.events.SlackNotification

class SlackNotificationTest : StringSpec({

    "send slack notification on publish".config(enabled = false) {
        SlackNotification.notifySlack(
            pageId = ""
        )
    }
})