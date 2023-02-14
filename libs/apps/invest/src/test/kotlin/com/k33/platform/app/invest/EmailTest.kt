package com.k33.platform.app.invest

import io.kotest.core.spec.style.StringSpec

class EmailTest: StringSpec({

    "send email".config(enabled = false) {
        InvestService.sendEmail(
            investorEmail = "test@k33.com",
            fundInfoRequest = FundInfoRequest(
                investorType = InvestorType.PROFESSIONAL,
                name = "Test",
                phoneNumber = PhoneNumber(
                    countryCode = "47",
                    nationalNumber = "12345678",
                ),
                countryCode = ISO3CountyCode.NOR,
                fundName = "K33 Assets I Fund Limited"
            )
        )
    }
})