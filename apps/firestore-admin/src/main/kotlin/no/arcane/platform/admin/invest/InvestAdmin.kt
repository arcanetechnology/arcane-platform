package no.arcane.platform.admin.invest

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.arcane.platform.app.invest.InvestService.delete
import no.arcane.platform.identity.auth.gcp.FirebaseAuthService
import no.arcane.platform.user.UserId

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