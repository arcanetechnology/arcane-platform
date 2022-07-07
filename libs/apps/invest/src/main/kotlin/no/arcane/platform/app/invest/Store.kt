package no.arcane.platform.app.invest

import io.firestore4k.typed.div
import no.arcane.platform.user.UserId
import no.arcane.platform.user.users

data class AppId(val value: String) {
    override fun toString(): String = value
}

val INVEST_APP = AppId("invest")

@kotlinx.serialization.Serializable
data class InvestApp(
    val status: Status
)

val apps = users.subCollection<InvestApp, AppId>("apps")
val fundInfoRequests = apps.subCollection<FundInfoRequest, String>("fund-info-requests")
val history = fundInfoRequests.subCollection<FundInfoRequest, String>("history")

fun UserId.inInvestAppContext() = users / this / apps / INVEST_APP