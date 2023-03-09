package com.k33.platform.admin

import com.k33.platform.admin.invest.InvestAdmin
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        InvestAdmin.deleteInvestApp()
    }
}