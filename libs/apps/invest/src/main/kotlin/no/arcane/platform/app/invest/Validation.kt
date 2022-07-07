package no.arcane.platform.app.invest

import com.google.i18n.phonenumbers.PhoneNumberUtil
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
    return phoneNumberUtil.isValidNumber(
        phoneNumberUtil.parse("$this", "")
    )
}