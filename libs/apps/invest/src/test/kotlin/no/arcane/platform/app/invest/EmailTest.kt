package no.arcane.platform.app.invest

import io.kotest.core.spec.style.StringSpec
import no.arcane.platform.app.invest.InvestService.sendEmail

class EmailTest: StringSpec({

    "send email".config(enabled = false) {
        FundInfoRequest(
            investorType = InvestorType.PROFESSIONAL,
            name = "Test",
            phoneNumber = PhoneNumber(
                countryCode = "47",
                nationalNumber = "12345678",
            ),
            countryCode = ISO3CountyCode.NOR,
            fundName = "Arcane Assets Fund Limited"
        ).sendEmail(
            investorEmail = "test@arcane.no"
        )
    }
})