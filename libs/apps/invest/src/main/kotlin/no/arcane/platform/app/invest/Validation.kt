package no.arcane.platform.app.invest

import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.arcane.platform.utils.logging.getLogger
import java.util.*

fun FundInfoRequest.validate(): List<String> {
    val errors = mutableListOf<String>()
    if (investorType != InvestorType.NON_PROFESSIONAL) {
        if (name.isNullOrBlank()) {
            errors += "Name is mandatory"
        }
        if (phoneNumber == null) {
            errors += "Phone number is mandatory"
        } else if (!phoneNumber.validate()) {
            errors += "Invalid phone number: $phoneNumber"
        }
        if (countryCode == null) {
            errors += "Country is mandatory"
        }
        if (fundName.isNullOrBlank()) {
            errors += "Fund name is mandatory"
        }
    }
    return errors
}

fun PhoneNumber.validate(): Boolean {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    return try {
        phoneNumberUtil.isValidNumber(
            phoneNumberUtil.parse("$this", "")
        )
    } catch (e: Exception) {
        val logger by getLogger()
        logger.warn("Exception: ${e.message} in parsing phone number: $this")
        false
    }
}

@Serializable
data class Country(
    val isO2CountyCode: String,
    val isO3CountyCode: String,
    val displayName: String,
    val callingCountryCode: Int
)
fun main() {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    val countryList = phoneNumberUtil
        .supportedRegions
        .map { iso2CountryCode ->
            val locale = Locale("", iso2CountryCode)
            Country(
                isO2CountyCode = iso2CountryCode,
                isO3CountyCode = try { locale.isO3Country } catch (e: Exception) {""},
                displayName = locale.displayName,
                callingCountryCode = phoneNumberUtil.getCountryCodeForRegion(iso2CountryCode)
            )
        }
        .sortedBy(Country::displayName)
    println(Json.encodeToString(countryList))
}