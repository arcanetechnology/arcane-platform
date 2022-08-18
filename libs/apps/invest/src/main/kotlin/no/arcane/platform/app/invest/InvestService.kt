package no.arcane.platform.app.invest

import io.firestore4k.typed.add
import io.firestore4k.typed.delete
import io.firestore4k.typed.div
import io.firestore4k.typed.get
import io.firestore4k.typed.put
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    private val emailFrom by lazy {
        config.email.from.toEmail()
    }

    private val emailToList by lazy {
        config.email.toList.toMandatoryEmailList()
    }

    private val emailCcList by lazy {
        config.email.ccList.toEmailList()
    }

    private val emailBccList by lazy {
        config.email.bccList.toEmailList()
    }

    private val emailService by getEmailService()

    fun getAllFundIds() = config.funds.keys

    fun FundInfoRequest.isApproved(fundId: FundId): Boolean {
        if (investorType == InvestorType.NON_PROFESSIONAL) {
            logger.info("Non-professional investor")
            return false
        }

        if (deniedCountryCodeList.contains(countryCode)) {
            logger.info("$countryCode is in denied list of countries")
            return false
        }

        if (config.funds[fundId.value]?.equals(fundName, ignoreCase = true) != true) {
            logger.info("Incorrect fund name")
            return false
        }

        return true
    }

    suspend fun UserId.getAllFunds(): Map<FundId, Fund?> = coroutineScope {
        config
            .funds
            .keys
            .map(::FundId)
            .associateWithAsync { fundId ->
                get(inInvestAppContext() / funds / fundId)
            }
    }

    suspend fun UserId.getFund(fundId: FundId): Fund? = get(inInvestAppContext() / funds / fundId)

    private suspend fun <K, V> List<K>.associateWithAsync(valueSelector: suspend (K) -> V): Map<K, V> {
        return coroutineScope {
            this@associateWithAsync.map { key ->
                async {
                    key to valueSelector(key)
                }
            }
                .awaitAll()
                .toMap()
        }
    }

    suspend fun UserId.saveStatus(
        fundId: FundId,
        status: Status,
    ) = put(inInvestAppContext() / funds / fundId, Fund(status))

    suspend fun UserId.saveFundInfoRequest(
        fundId: FundId,
        fundInfoRequest: FundInfoRequest,
    ) = add(inInvestAppContext() / funds / fundId / fundInfoRequests, fundInfoRequest)

    suspend fun sendEmail(
        investorEmail: String,
        fundInfoRequest: FundInfoRequest,
    ) = emailService.sendEmail(
        from = emailFrom,
        toList = emailToList,
        ccList = emailCcList,
        bccList = emailBccList,
        subject = "Arcane Fund Inquiry Request",
        contentType = ContentType.MONOSPACE_TEXT,
        body = fundInfoRequest.asString(
            investorEmail = investorEmail,
        ),
    )

    suspend fun sendSlackNotification(
        investorEmail: String,
        fundInfoRequest: FundInfoRequest,
    ) = SlackNotification.notifySlack(
        strFundInfoRequest = fundInfoRequest.asString(
            investorEmail = investorEmail,
        ),
    )

    internal fun FundInfoRequest.asString(
        investorEmail: String,
    ): String = """
        Details of Investor submitting inquiry for the Arcane Fund.
    
        Investor category ..... ${investorType.label}
        Full Name ............. $name
        Company ............... ${company ?: "-"}
        E-mail ................ $investorEmail
        Phone ................. $phoneNumber
        Country of residence .. ${countryCode?.let { "${it.displayName} (${it.name})" }}
        Name of fund .......... $fundName

        Action taken .......... Approved
        """.trimIndent()

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun UserId.delete() = delete(inInvestAppContext())
}

