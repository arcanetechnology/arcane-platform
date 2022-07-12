package no.arcane.platform.app.invest

import io.firestore4k.typed.div
import io.ktor.server.plugins.*
import no.arcane.platform.user.UserId
import no.arcane.platform.user.users
import kotlinx.serialization.Serializable

data class AppId(val value: String) {
    override fun toString(): String = value
}

val INVEST_APP = AppId("invest")

data class FundId(val value: String) {
    init {
        if (InvestService.getAllFundIds().contains(value).not()) {
            throw NotFoundException("Fund: $value not found")
        }
    }
    override fun toString(): String = value
}

@Serializable
data class Fund(
    val status: Status
)

val apps = users.subCollection<Unit, AppId>("apps")
val funds = apps.subCollection<Fund, FundId>("funds")
val fundInfoRequests = funds.subCollection<FundInfoRequest, String>("fund-info-requests")

fun UserId.inInvestAppContext() = users / this / apps / INVEST_APP