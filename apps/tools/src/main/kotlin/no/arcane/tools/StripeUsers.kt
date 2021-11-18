package no.arcane.tools

import java.io.File

data class Domain(
    val fqdn: String,
    val withoutTLD: String,
)

fun main() {
    val emailDomainList = File("src/main/resources/customers.csv")
        .readLines()
        .asSequence()
        .map {
            Domain(
                fqdn = it.split("@")[1].lowercase(),
                withoutTLD = it.split("@")[1]
                    .split(".")[0]
                    .lowercase()
                    .replace("live", "outlook")
                    .replace("hotmail", "outlook")
                    .replace("me", "icloud")
                    .replace("googlemail", "gmail")
            )
        }
        .groupBy { it.withoutTLD }
        .toList()
        .sortedBy { it.first }
        .sortedByDescending { it.second.count() }
        .toList()

    println("Total emails: ${emailDomainList.sumOf { it.second.count() }}")
    println("Total domains: ${emailDomainList.size}")
    println("Unique domains: ${emailDomainList.count { it.second.count() == 1 }}")

    println()
    emailDomainList.forEach { println("${it.first} - ${it.second.count()} - ${it.second.map(Domain::fqdn).groupingBy { it }.eachCount()}") }
}