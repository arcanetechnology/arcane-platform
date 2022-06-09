package no.arcane.platform.tests.trade

import io.kotest.core.spec.style.BehaviorSpec
import no.arcane.platform.apps.trade.test.setupSpannerWithTestData
import no.arcane.platform.tests.utils.tradeSpannerDdlFile

class TradeAdminTest : BehaviorSpec({

    setupSpannerWithTestData(ddlFile = tradeSpannerDdlFile)

    // this has to be first since it needs to check custody accounts balances before any other tests
    custodyAccountTests()
    cryptoCustodyAccountTests()

    userTests()

    profileTests()

    accountTests()
    cryptoAccountTests()

    transactionTests()
})