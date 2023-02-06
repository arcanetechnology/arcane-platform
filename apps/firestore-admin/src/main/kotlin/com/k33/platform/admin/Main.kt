package com.k33.platform.admin

import kotlinx.coroutines.runBlocking
import com.k33.platform.admin.invest.InvestAdmin

fun main() {
    runBlocking {
        InvestAdmin.deleteInvestApp()
    }
}