package no.arcane.platform.app.trade.utils.virtualaccount


data class AdtContext(
    private val separator: Char,
) {
    operator fun String.times(string: String) = listOf(this) * listOf(string)

    operator fun String.times(list: List<String>) = listOf(this) * list

    operator fun List<String>.times(string: String) = this * listOf(string)
    operator fun List<String>.times(list: List<String>): List<String> {
        return this.flatMap { first ->
            list.map { second -> "$first$separator$second" }
        }
    }
}


