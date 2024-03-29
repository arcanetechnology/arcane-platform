package no.arcane.platform.app.invest

import io.kotest.core.spec.style.StringSpec
import no.arcane.platform.app.invest.InvestService.asString

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
                fundName = "Arcane Assets Fund Limited"
            ).asString(investorEmail = "test@arcane.no"),
            testMode = true,
        )
    }
})