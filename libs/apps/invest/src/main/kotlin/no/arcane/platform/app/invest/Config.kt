package no.arcane.platform.app.invest

data class Config(
    val deniedCountryCodeList: String,
    val funds: Map<String, String>,
    val email: Email,
)
data class Email(
    val from: String,
    val to: String,
)