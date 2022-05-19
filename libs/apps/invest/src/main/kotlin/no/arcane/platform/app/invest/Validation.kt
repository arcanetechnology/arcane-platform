package no.arcane.platform.app.invest

import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.*

fun FundInfoRequest.validate(): List<String> {
    val errors = mutableListOf<String>()
    if (!phoneNumber.validate()) {
        errors += "Invalid phone number: $phoneNumber"
    }
    return errors
}

fun PhoneNumber.validate(): Boolean {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    return phoneNumberUtil.isValidNumber(
        phoneNumberUtil.parse("$this", "")
    )
}