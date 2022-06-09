package no.arcane.platform.app.trade.ledger.db.spanner.account

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.right
import com.google.cloud.Timestamp
import com.google.cloud.spanner.Mutation
import com.google.cloud.spanner.Statement
import com.google.cloud.spanner.TransactionContext
import no.arcane.platform.app.trade.admin.api.rest.AccountOperation
import no.arcane.platform.app.trade.admin.api.rest.readAccountOperations
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCurrency
import no.arcane.platform.app.trade.ledger.db.spanner.Currency
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCurrency
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioCryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.TransactionId
import no.arcane.platform.app.trade.ledger.db.spanner.VirtualAccount
import no.arcane.platform.app.trade.ledger.db.spanner.VirtualAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.queryStatement
import java.time.Instant
import java.util.*

object VirtualAccountStore {

    fun get(virtualAccountId: VirtualAccountId) : Either<String, VirtualAccount> {
        val currencyString = virtualAccountId.value.substringAfterLast('-').uppercase()
        val currency: Currency = try {
            FiatCurrency.valueOf(currencyString)
        } catch (e: Exception) {
            CryptoCurrency(currencyString)
        }
        return VirtualAccount(
            id = virtualAccountId,
            currency = currency,
            alias = currencyString.replace('-',' ')
        ).right()
    }

    suspend fun operate(
        transactionContext: TransactionContext,
        transactionId: TransactionId,
        virtualAccountId: VirtualAccountId,
        amount: Long,
        currency: Currency,
        timestamp: Timestamp,
    ): Either<String, Unit> {
        return either {
            transactionContext.buffer(
                Mutation
                    .newInsertBuilder("VirtualAccountOperations")
                    .set("TransactionId").to(transactionId.value)
                    .set("VirtualAccountId").to(virtualAccountId.value)
                    .set("Amount").to(amount)
                    .set("Currency").to(currency.toText())
                    .set("CreatedOn").to(timestamp)
                    .build(),
            )
        }
    }
}