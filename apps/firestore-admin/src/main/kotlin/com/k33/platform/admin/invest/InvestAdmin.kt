package com.k33.platform.admin.invest

import com.k33.platform.app.invest.InvestService.delete
import com.k33.platform.identity.auth.gcp.FirebaseAuthService
import com.k33.platform.user.UserId
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

object InvestAdmin {
    suspend fun deleteInvestApp(
        vararg userEmails: String
    ) {
        coroutineScope {
            userEmails
                .mapNotNull { FirebaseAuthService.findUserIdOrNull(it) }
                .map(::UserId)
                .map {
                    async {
                        it.delete()
                    }
                }.awaitAll()
        }
    }
}