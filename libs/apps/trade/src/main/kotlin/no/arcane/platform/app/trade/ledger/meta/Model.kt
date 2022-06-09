package no.arcane.platform.app.trade.ledger.meta

import arrow.core.ValidatedNel
import arrow.core.invalid
import arrow.core.valid
import no.arcane.platform.utils.arrow.validated

data class Transaction(
    val operations: List<Operation>
)

data class Operation(
    val account: Account,
    val amount: Long,
)

enum class AccountType {
    STAKEHOLDER,
    EXTERNAL,
}

data class Account(
    val accountId: String,
    val type: AccountType,
)

data class Currency(val id: String)

fun Account.getCurrency(): Currency {
    return Currency("")
}

fun Account.getUser(): String? {
    return null
}

sealed class TransactionValidationError

data class AccessDenied(
    val userId: String,
    val accountId: String,
) : TransactionValidationError()

data class NonZeroSumForOperations(
    val currency: Currency,
    val invalidOperations: List<Operation>,
) : TransactionValidationError()

suspend fun validateTransaction(
    userId: String,
    transaction: Transaction,
): ValidatedNel<TransactionValidationError, Transaction> {
    return validated {
        transaction.operations.groupBy { it.account.getCurrency() }
            .forEach { (currency, operations) ->
                if (operations.sumOf { it.amount } != 0L) {
                    NonZeroSumForOperations(
                        currency = currency,
                        invalidOperations = operations
                    ).invalid().bind()
                }
            }
        transaction.operations.partition {
            it.amount > 0 || it.account.getUser() == userId
        }.let { (validOperations, invalidOperations) ->
            validOperations.valid()
            invalidOperations.map { (account, _) ->
                AccessDenied(
                    userId = userId,
                    accountId = account.accountId
                ).invalid().bind()
            }
        }
        transaction
    }
}




