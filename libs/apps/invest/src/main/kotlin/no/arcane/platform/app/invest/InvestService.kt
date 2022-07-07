package no.arcane.platform.app.invest

import io.firestore4k.typed.add
import io.firestore4k.typed.div
import io.firestore4k.typed.get
import io.firestore4k.typed.put
import no.arcane.platform.email.ContentType
import no.arcane.platform.email.getEmailService
import no.arcane.platform.user.UserId
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger

object InvestService {

    private val logger by getLogger()

    private val config by loadConfig<Config>("invest", "invest")

    private val deniedCountryCodeList by lazy {
        config.deniedCountryCodeList
            .uppercase()
            .split(',')
            .map(ISO3CountyCode::valueOf)
    }

    private val emailService by getEmailService()

    fun FundInfoRequest.isApproved(): Boolean {
        if (investorType == InvestorType.NON_PROFESSIONAL) {
            logger.info("Unqualified investor")
            return false
        }

        if (deniedCountryCodeList.contains(countryCode)) {
            logger.info("$countryCode is in denied list of countries")
            return false
        }

        if (!fundName.equals(config.fundName, ignoreCase = true)) {
            logger.info("Incorrect fund name")
            return false
        }

        return true
    }

    suspend fun UserId.getStatus(): Status = get(inInvestAppContext())?.status ?: Status.NOT_REGISTERED

    suspend fun UserId.saveStatus(
        status: Status,
    ) {
        put(inInvestAppContext(), InvestApp(status))
    }

    suspend fun UserId.saveFundInfoRequest(
        fundInfoRequest: FundInfoRequest,
    ) {
        put(inInvestAppContext() / fundInfoRequests / "latest", fundInfoRequest)
        add(inInvestAppContext() / fundInfoRequests / "latest" / history, fundInfoRequest)
    }

    suspend fun FundInfoRequest.sendEmail(
        to: String,
    ) {
        emailService.sendEmail(
            from = config.email.from,
            to = config.email.to,
            subject = "Arcane Fund Inquiry Request",
            contentType = ContentType.MONOSPACE_TEXT,
            body = """
            Details of Investor submitting inquiry for the Arcane Fund.

            Investor category ..... ${investorType.label}
            Full Name ............. $name
            Company ............... ${company ?: "-"}
            E-mail ................ $to
            Phone ................. $phoneNumber
            Country of residence .. ${countryCode?.let { "${it.displayName} (${it.name})" }}
            Name of fund .......... $fundName

            Action taken .......... Approved
            """.trimIndent()
        )
    }
}