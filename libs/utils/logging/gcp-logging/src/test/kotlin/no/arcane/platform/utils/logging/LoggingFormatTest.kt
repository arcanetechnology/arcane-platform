package no.arcane.platform.utils.logging

import io.kotest.core.spec.style.StringSpec

class LoggingFormatTest : StringSpec({

    val logger by getLogger()

    "Logging in GCP stack-driver layout " {
        logWithMDC("userId" to "test-user") {
            logger.info("info level message")
            logger.warn("warn level message")
            logger.error("error level message")
        }
    }
})