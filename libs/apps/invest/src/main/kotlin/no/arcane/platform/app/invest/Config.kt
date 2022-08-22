package no.arcane.platform.app.invest

import no.arcane.platform.email.Email
import java.lang.Exception

data class Config(
    val deniedCountryCodeList: String,
    val funds: Map<String, String>,
    val testDomain: String,
    val email: EmailSetting,
)

data class EmailSetting(
    val from: String,
    val toList: String,
    val ccList: String? = null,
    val bccList: String? = null,
)

fun String.toEmail(): Email {
    return if (contains('#')) {
        val (address, label) = split('#')
        Email(address = address, label = label)
    } else {
        Email(this)
    }
}

fun String?.toEmailList(): List<Email> = this
    ?.split(',')
    ?.filterNot { it.isBlank() }
    ?.map(String::toEmail)
    ?: emptyList()

fun String.toMandatoryEmailList(): List<Email> {
    if (isBlank()) {
        throw Exception("Mandatory list cannot be empty")
    }
    return this.toEmailList()
}
