package no.arcane.platform.app.invest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ValidatePhoneNumberTest: StringSpec({

    "Test valid phone number" {
        PhoneNumber("47", "41234567").validate() shouldBe true
    }
    "Test invalid phone number" {
        PhoneNumber("47", "123456789").validate() shouldBe false
    }
})