package no.arcane.platform.app.trade.utils.virtualaccount

import kotlinx.serialization.Serializable

data class VirtualAccountType(
    val owners: List<String>,
    val currencies: List<String>,
    val allowNegative: Boolean = true,
) {
    constructor(
        owner: String,
        currencies: List<String>,
        allowNegative: Boolean = true,
    ) : this(listOf(owner), currencies, allowNegative)
}

@Serializable
data class VirtualAccountOption(
    val id: String,
    val label: String,
    val currency: String,
    val allowNegative: Boolean,
)