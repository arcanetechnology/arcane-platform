package com.k33.platform.utils.logging

import io.kotest.core.spec.style.StringSpec

val NOTIFY_SLACK_ALERTS = NotifySlack.NOTIFY_SLACK_ALERTS.getMarker()

class NotifySlackFilterTest : StringSpec({
    val logger by getLogger()
    "send log message with NOTIFY_SLACK marker to slack".config(enabled = false) {
        logWithMDC(
            "userId" to "test-user",
            "env" to "test"
        ) {
            logger.info(NOTIFY_SLACK_ALERTS, "This is information message")
            logger.warn(NOTIFY_SLACK_ALERTS, "This is warning message")
            logger.error(NOTIFY_SLACK_ALERTS, "This is error message")
        }
    }
})