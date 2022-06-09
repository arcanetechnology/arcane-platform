package no.arcane.platform.utils.arrow

import arrow.core.ValidatedNel
import arrow.core.invalid
import arrow.core.nonEmptyListOf
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*

data class Identity(
    val name: String,
    val age: Int,
    val countryCode: String,
)

sealed class InvalidIdentity(
    val errorMessage: String
) {
    object BlankName : InvalidIdentity("name is blank")

    data class LowAge(private val age: Int) : InvalidIdentity("age($age) < 18")

    object BlankCountryCode : InvalidIdentity("country code is blank")

    data class CountryCodeNotFound(private val code: String) : InvalidIdentity("country code($code) not found")
}

suspend fun validate(
    identity: Identity
): ValidatedNel<InvalidIdentity, Identity> {
    return validated {
        if (identity.name.isBlank()) {
            InvalidIdentity.BlankName.invalid().bind()
        }
        if (identity.age < 18) {
            InvalidIdentity.LowAge(identity.age).invalid().bind()
        }
        if (identity.countryCode.isBlank()) {
            InvalidIdentity.BlankCountryCode.invalid().bind()
        }
        if (!Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3).contains(identity.countryCode.uppercase())) {
            InvalidIdentity.CountryCodeNotFound(identity.countryCode).invalid().bind()
        }
        identity
    }
}

class ValidatedTest: StringSpec({
    "Test validate { Validated.bind() }" {
        validate(
            Identity(
                name = "",
                age = 0,
                countryCode = ""
            )
        ).fold(
            { errors ->
                errors shouldBe nonEmptyListOf(
                    InvalidIdentity.BlankName,
                    InvalidIdentity.LowAge(0),
                    InvalidIdentity.BlankCountryCode,
                    InvalidIdentity.CountryCodeNotFound(""),
                )
            },
            { fail("Should not be Valid") }
        )
    }
})