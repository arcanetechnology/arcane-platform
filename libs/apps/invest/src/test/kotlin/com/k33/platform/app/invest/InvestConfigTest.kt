package com.k33.platform.app.invest

import com.k33.platform.utils.config.loadConfig
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class InvestConfigTest : StringSpec({

    val config by loadConfig<Config>("invest", "invest")

    "verify config.email.from" {
        // println("config.email.from: ${config.email.from}")
        config.email.from shouldBe System.getenv("INVEST_EMAIL_FROM")
    }

    "verify config.email.toList" {
        // println("config.email.toList: ${config.email.toList}")
        config.email.toList shouldBe System.getenv("INVEST_EMAIL_TO_LIST")
    }

    "verify config.email.ccList" {
        // println("config.email.ccList: ${config.email.ccList}")
        config.email.ccList shouldBe System.getenv("INVEST_EMAIL_CC_LIST")
    }

    "verify config.email.bccList" {
        // println("config.email.bccList: ${config.email.bccList}")
        config.email.bccList shouldBe System.getenv("INVEST_EMAIL_BCC_LIST")
    }
})