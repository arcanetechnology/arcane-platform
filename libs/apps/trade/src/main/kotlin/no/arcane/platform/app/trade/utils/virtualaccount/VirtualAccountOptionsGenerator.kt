package no.arcane.platform.app.trade.utils.virtualaccount

fun List<VirtualAccountType>.toVirtualAccountOptionList(): List<VirtualAccountOption> {
    return flatMap { virtualAccountType ->
        virtualAccountType.owners.flatMap { owner ->
            virtualAccountType.currencies.map { currency ->
                VirtualAccountOption(
                    id = "${owner}-external-${currency}".lowercase().replace(' ', '-'),
                    label = "$owner External $currency",
                    currency = currency,
                    allowNegative = virtualAccountType.allowNegative
                )
            }
        }
    }
}











