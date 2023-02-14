package com.k33.platform.app.invest

import io.kotest.core.spec.style.StringSpec
import com.k33.platform.app.invest.InvestService.asString

class SlackNotificationTest : StringSpec({

    "send slack notification on publish".config(enabled = false) {
        SlackNotification.notifySlack(
            FundInfoRequest(
                investorType = InvestorType.PROFESSIONAL,
                name = "Test",
                phoneNumber = PhoneNumber(
                    countryCode = "47",
                    nationalNumber = "12345678",
                ),
                countryCode = ISO3CountyCode.NOR,
                fundName = "K33 Assets I Fund Limited"
            ).asString(investorEmail = "test@k33.com"),
            testMode = true,
        )
    }
})