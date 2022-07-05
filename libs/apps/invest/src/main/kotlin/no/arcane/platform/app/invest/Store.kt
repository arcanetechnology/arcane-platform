package no.arcane.platform.app.invest

import io.firestore4k.typed.div
import no.arcane.platform.user.UserId
import no.arcane.platform.user.users

class Void
data class AppId(val value: String) {
    override fun toString(): String = value
}

val INVEST_APP = AppId("invest")

val apps = users.subCollection<Void, AppId>("apps")
val fundInfoRequests = apps.subCollection<FundInfoRequest, UserId>("fund-info-requests")
val history = fundInfoRequests.subCollection<FundInfoRequest, String>("history")

fun UserId.inInvestAppContext() = users / this / apps / INVEST_APP