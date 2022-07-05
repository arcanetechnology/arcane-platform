package no.arcane.platform.app.invest

import io.firestore4k.typed.add
import io.firestore4k.typed.div
import io.firestore4k.typed.get
import io.firestore4k.typed.put
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

    suspend fun UserId.isRegistered(): Boolean {
        val fundInfoRequest: FundInfoRequest? = get(inInvestAppContext() / fundInfoRequests / this)
        if (fundInfoRequest == null) {
            logger.info("Not registered")
            return false
        }
        return setOf(InvestorType.PROFESSIONAL, InvestorType.ELECTIVE_PROFESSIONAL)
            .contains(fundInfoRequest.investorType)
    }

    suspend fun UserId.register(
        fundInfoRequest: FundInfoRequest,
        email: String
    ): Boolean {
        if (!fundInfoRequest.fundName.equals(config.fundName, ignoreCase = true)) {
            logger.info("Incorrect fund name")
            return false
        }

        if (fundInfoRequest.investorType == InvestorType.UNQUALIFIED) {
            logger.info("Unqualified investor")
            return false
        }

        if (deniedCountryCodeList.contains(fundInfoRequest.countryCode)) {
            logger.info("${fundInfoRequest.countryCode} is in denied list of countries")
            return false
        }
        put(inInvestAppContext() / fundInfoRequests / this, fundInfoRequest)
        add(inInvestAppContext() / fundInfoRequests / this / history, fundInfoRequest)
        emailService.sendEmail(
            from = config.email.from,
            to = config.email.to,
            subject = "Arcane Fund Inquiry Request",
            body = """
            Details of Investor submitting inquiry for the Arcane Fund.

            Investor category ..... ${fundInfoRequest.investorType.label}
            Full Name ............. ${fundInfoRequest.name}
            Company ............... ${fundInfoRequest.company ?: "-"} 
            E-mail ................ $email 
            Phone ................. ${fundInfoRequest.phoneNumber} 
            Country of residence .. ${fundInfoRequest.countryCode.let { "${it.displayName} (${it.name})" }} 
            Name of fund .......... ${fundInfoRequest.fundName} 
            Action taken .......... Approved
            """.trimIndent()
        )
        return true
    }
}