package no.arcane.platform.admin

import kotlinx.coroutines.runBlocking
import no.arcane.platform.admin.invest.InvestAdmin

fun main() {
    runBlocking {
        InvestAdmin.deleteInvestApp()
    }
}