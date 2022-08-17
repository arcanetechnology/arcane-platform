package no.arcane.platform.app.invest

import io.kotest.core.spec.style.StringSpec

class EmailTest: StringSpec({

    "send email".config(enabled = false) {
        InvestService.sendEmail(
            investorEmail = "test@arcane.no",
            fundInfoRequest = FundInfoRequest(
                investorType = InvestorType.PROFESSIONAL,
                name = "Test",
                phoneNumber = PhoneNumber(
                    countryCode = "47",
                    nationalNumber = "12345678",
                ),
                countryCode = ISO3CountyCode.NOR,
                fundName = "Arcane Assets Fund Limited"
            )
        )
    }
})