package no.arcane.platform.app.trade.ledger.db.spanner.transaction

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import com.google.cloud.Timestamp
import com.google.cloud.spanner.Mutation
import com.google.cloud.spanner.Statement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.arcane.platform.app.trade.admin.api.rest.Transaction
import no.arcane.platform.app.trade.ledger.db.spanner.Account
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountOperation
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoCustodyAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccountOperation
import no.arcane.platform.app.trade.ledger.db.spanner.CryptoStakeholderAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.CustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccountOperation
import no.arcane.platform.app.trade.ledger.db.spanner.FiatCustodyAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccountOperation
import no.arcane.platform.app.trade.ledger.db.spanner.FiatStakeholderAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.Operation
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioCryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioCryptoStakeholderAccountOperation
import no.arcane.platform.app.trade.ledger.db.spanner.PortfolioCryptoStakeholderAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.StakeholderAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.TransactionId
import no.arcane.platform.app.trade.ledger.db.spanner.VirtualAccountId
import no.arcane.platform.app.trade.ledger.db.spanner.VirtualAccountOperation
import no.arcane.platform.app.trade.ledger.db.spanner.VirtualAccountOperationId
import no.arcane.platform.app.trade.ledger.db.spanner.account.CryptoCustodyAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.account.CryptoStakeholderAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.account.FiatCustodyAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.account.FiatStakeholderAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.account.PortfolioCryptoStakeholderAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.account.VirtualAccountStore
import no.arcane.platform.app.trade.ledger.db.spanner.queryStatement
import no.arcane.platform.app.trade.ledger.db.spanner.readOnlyTransaction
import no.arcane.platform.app.trade.ledger.db.spanner.readWriteTransaction
import no.arcane.platform.app.trade.ledger.db.spanner.toInstant
import no.arcane.platform.utils.arrow.join
import java.time.Instant
import no.arcane.platform.app.trade.admin.api.rest.AddOperation as RestOperation

object TransactionStore {

    suspend fun add(
        transactionId: TransactionId,
        operations: List<RestOperation>,
    ): Either<List<String>, Transaction> {
        return readWriteTransaction { txn ->
            either {
                data class AccountOperation(
                    val account: Account,
                    val amount: Long,
                )

                val accountOperations = operations
                    .map { operation ->
                        when (operation.accountId) {
                            is VirtualAccountId -> VirtualAccountStore.get(
                                operation.accountId
                            )

                            is FiatCustodyAccountId -> FiatCustodyAccountStore.get(
                                txn,
                                operation.accountId
                            )

                            is CryptoCustodyAccountId -> CryptoCustodyAccountStore.get(
                                txn,
                                operation.accountId
                            )

                            is CryptoStakeholderAccountId -> CryptoStakeholderAccountStore.get(
                                txn,
                                operation.accountId
                            )

                            is FiatStakeholderAccountId -> FiatStakeholderAccountStore.get(
                                txn,
                                operation.accountId
                            )

                            is PortfolioCryptoStakeholderAccountId -> PortfolioCryptoStakeholderAccountStore.get(
                                txn,
                                operation.accountId
                            )
                        }.map { account ->
                            AccountOperation(
                                account = account,
                                amount = operation.amount,
                            )
                        }
                    }
                    .join()
                    .bind()
                val errors = mutableListOf<String>()
                accountOperations
                    .groupBy { it.account.id }
                    .forEach { (accountId, accountOperationsForId) ->
                        if (accountOperationsForId.size > 1) {
                            errors += "Multiple operations on account: ${Json.encodeToString(accountId)}"
                        }
                    }
                accountOperations
                    .forEachIndexed { index, accountOperation ->
                        if (accountOperation.amount == 0L) {
                            errors += "For operation: $index, amount is zero."
                        }
                    }
                accountOperations
                    .forEachIndexed { index, accountOperation ->
                        if (accountOperation.account.id !is VirtualAccountId
                            && accountOperation.amount < 0
                            && accountOperation.account.balance < -accountOperation.amount
                        ) {
                            errors += "For operation: $index, negative debit amount: ${accountOperation.amount} is less than account balance: ${accountOperation.account.balance}."
                        }
                    }

                accountOperations
                    .filter { it.account.id !is CustodyAccountId }
                    .groupBy { it.account.currency }
                    .forEach { (ccy, accountOperationsForCcy) ->
                        val sum = accountOperationsForCcy.sumOf(AccountOperation::amount)
                        if (sum != 0L) {
                            errors += "For currency: $ccy, sum of amounts: $sum is not zero."
                        }
                    }

                accountOperations
                    .filter { it.account.id !is StakeholderAccountId }
                    .groupBy { it.account.currency }
                    .forEach { (ccy, accountOperationsForCcy) ->
                        val sum = accountOperationsForCcy.sumOf(AccountOperation::amount)
                        if (sum != 0L) {
                            errors += "For currency: $ccy, sum of amounts: $sum is not zero."
                        }
                    }

                if (errors.isNotEmpty()) {
                    errors.left().bind<Unit>()
                }

                // TODO verify custody account operations

                val now = Timestamp.now()
                txn.buffer(
                    Mutation.newInsertBuilder("Transactions")
                        .set("TransactionId").to(transactionId.value)
                        .set("CreatedOn").to(now)
                        .build()
                )
                operations
                    .map { operation ->
                        when (operation.accountId) {
                            is VirtualAccountId -> VirtualAccountStore.operate(
                                transactionContext = txn,
                                virtualAccountId = operation.accountId,
                                transactionId = transactionId,
                                amount = operation.amount,
                                currency = accountOperations.first { it.account.id == operation.accountId }.account.currency,
                                timestamp = now,
                            )

                            is FiatCustodyAccountId -> FiatCustodyAccountStore.operate(
                                transactionContext = txn,
                                fiatCustodyAccountId = operation.accountId,
                                transactionId = transactionId,
                                amount = operation.amount,
                                timestamp = now,
                            )

                            is CryptoCustodyAccountId -> CryptoCustodyAccountStore.operate(
                                transactionContext = txn,
                                cryptoCustodyAccountId = operation.accountId,
                                transactionId = transactionId,
                                amount = operation.amount,
                                timestamp = now,
                            )

                            is FiatStakeholderAccountId -> FiatStakeholderAccountStore.operate(
                                transactionContext = txn,
                                fiatStakeholderAccountId = operation.accountId,
                                transactionId = transactionId,
                                amount = operation.amount,
                                timestamp = now,
                            )

                            is CryptoStakeholderAccountId -> CryptoStakeholderAccountStore.operate(
                                transactionContext = txn,
                                cryptoStakeholderAccountId = operation.accountId,
                                transactionId = transactionId,
                                amount = operation.amount,
                                timestamp = now,
                            )

                            is PortfolioCryptoStakeholderAccountId -> PortfolioCryptoStakeholderAccountStore.operate(
                                transactionContext = txn,
                                portfolioCryptoStakeholderAccountId = operation.accountId,
                                transactionId = transactionId,
                                amount = operation.amount,
                                timestamp = now,
                            )
                        }
                    }
                    .join()
                    .bind()
                Transaction(
                    id = transactionId.value,
                    createdOn = now.toInstant().toString()
                )
            }
        }
    }

    suspend fun get(
        offset: Instant,
        limit: ULong
    ): Either<String, List<Transaction>> {
        return queryStatement(
            Statement.newBuilder("""
                SELECT *
                FROM Transactions
                WHERE CreatedOn > @offset
                LIMIT @limit
                """.trimIndent()
            )
                .bind("offset").to(Timestamp.ofTimeSecondsAndNanos(offset.epochSecond, offset.nano))
                .bind("limit").to(limit.toLong())
                .build()
        ) { resultSet ->
            val transactions = mutableListOf<Transaction>()
            while (resultSet.next()) {
                transactions += Transaction(
                    id = resultSet.getString("TransactionId"),
                    createdOn = resultSet.getTimestamp("CreatedOn").toInstant().toString(),
                )
            }
            transactions.right()
        }
    }

    suspend fun get(
        transactionId: TransactionId
    ): Either<String, Transaction> {
        return queryStatement(
            Statement.newBuilder("""
                SELECT *
                FROM Transactions
                WHERE TransactionId = @transactionId
                """.trimIndent()
            )
                .bind("transactionId").to(transactionId.value)
                .build()
        ) { resultSet ->
            if (resultSet.next()) {
                Transaction(
                    id = resultSet.getString("TransactionId"),
                    createdOn = resultSet.getTimestamp("CreatedOn").toInstant().toString(),
                ).right()
            } else {
                "Transaction not found".left()
            }
        }
    }

    suspend fun getOperations(transactionId: TransactionId): Either<String, List<Operation>> {
        return readOnlyTransaction { txn ->
            val operations = mutableListOf<Operation>()
            txn.executeQuery(
                Statement.newBuilder(
                    """
                    SELECT *
                    FROM FiatCustodyAccountOperations
                    WHERE TransactionId = @transactionId
                    """.trimIndent()
                )
                    .bind("transactionId").to(transactionId.value)
                    .build()
            ).use { resultSet ->
                while (resultSet.next()) {
                    operations += FiatCustodyAccountOperation(
                        id = FiatCustodyAccountOperationId(
                            accountId = FiatCustodyAccountId(resultSet.getString("FiatCustodyAccountId")),
                            transactionId = TransactionId(resultSet.getString("TransactionId")),
                        ),
                        amount = resultSet.getLong("Amount"),
                        balance = resultSet.getLong("Balance"),
                        createdOn = resultSet.getTimestamp("CreatedOn").toInstant(),
                    )
                }
            }
            txn.executeQuery(
                Statement.newBuilder(
                    """
                    SELECT *
                    FROM CryptoCustodyAccountOperations
                    WHERE TransactionId = @transactionId
                    """.trimIndent()
                )
                    .bind("transactionId").to(transactionId.value)
                    .build()
            ).use { resultSet ->
                while (resultSet.next()) {
                    operations += CryptoCustodyAccountOperation(
                        id = CryptoCustodyAccountOperationId(
                            accountId = CryptoCustodyAccountId(resultSet.getString("CryptoCustodyAccountId")),
                            transactionId = TransactionId(resultSet.getString("TransactionId")),
                        ),
                        amount = resultSet.getLong("Amount"),
                        balance = resultSet.getLong("Balance"),
                        createdOn = resultSet.getTimestamp("CreatedOn").toInstant(),
                    )
                }
            }
            txn.executeQuery(
                Statement.newBuilder(
                    """
                    SELECT *
                    FROM FiatStakeholderAccountOperations
                    WHERE TransactionId = @transactionId
                    """.trimIndent()
                )
                    .bind("transactionId").to(transactionId.value)
                    .build()
            ).use { resultSet ->
                while (resultSet.next()) {
                    operations += FiatStakeholderAccountOperation(
                        id = FiatStakeholderAccountOperationId(
                            accountId = FiatStakeholderAccountId(
                                userId = resultSet.getString("UserId"),
                                profileId = resultSet.getString("ProfileId"),
                                value = resultSet.getString("FiatStakeholderAccountId"),
                            ),
                            transactionId = TransactionId(resultSet.getString("TransactionId")),
                        ),
                        amount = resultSet.getLong("Amount"),
                        balance = resultSet.getLong("Balance"),
                        createdOn = resultSet.getTimestamp("CreatedOn").toInstant(),
                    )
                }
            }
            txn.executeQuery(
                Statement.newBuilder(
                    """
                    SELECT *
                    FROM CryptoStakeholderAccountOperations
                    WHERE TransactionId = @transactionId
                    """.trimIndent()
                )
                    .bind("transactionId").to(transactionId.value)
                    .build()
            ).use { resultSet ->
                while (resultSet.next()) {
                    operations += CryptoStakeholderAccountOperation(
                        id = CryptoStakeholderAccountOperationId(
                            accountId = CryptoStakeholderAccountId(
                                userId = resultSet.getString("UserId"),
                                profileId = resultSet.getString("ProfileId"),
                                value = resultSet.getString("CryptoStakeholderAccountId"),
                            ),
                            transactionId = TransactionId(resultSet.getString("TransactionId")),
                        ),
                        amount = resultSet.getLong("Amount"),
                        balance = resultSet.getLong("Balance"),
                        createdOn = resultSet.getTimestamp("CreatedOn").toInstant(),
                    )
                }
            }
            txn.executeQuery(
                Statement.newBuilder(
                    """
                    SELECT *
                    FROM PortfolioCryptoStakeholderAccountOperations
                    WHERE TransactionId = @transactionId
                    """.trimIndent()
                )
                    .bind("transactionId").to(transactionId.value)
                    .build()
            ).use { resultSet ->
                while (resultSet.next()) {
                    operations += PortfolioCryptoStakeholderAccountOperation(
                        id = PortfolioCryptoStakeholderAccountOperationId(
                            accountId = PortfolioCryptoStakeholderAccountId(
                                userId = resultSet.getString("UserId"),
                                profileId = resultSet.getString("ProfileId"),
                                accountId = resultSet.getString("FiatStakeholderAccountId"),
                                portfolioId = resultSet.getString("PortfolioId"),
                                value = resultSet.getString("PortfolioCryptoStakeholderAccountId"),
                            ),
                            transactionId = TransactionId(resultSet.getString("TransactionId")),
                        ),
                        amount = resultSet.getLong("Amount"),
                        balance = resultSet.getLong("Balance"),
                        createdOn = resultSet.getTimestamp("CreatedOn").toInstant(),
                    )
                }
            }
            txn.executeQuery(
                Statement.newBuilder(
                    """
                    SELECT *
                    FROM VirtualAccountOperations
                    WHERE TransactionId = @transactionId
                    """.trimIndent()
                )
                    .bind("transactionId").to(transactionId.value)
                    .build()
            ).use { resultSet ->
                while (resultSet.next()) {
                    operations += VirtualAccountOperation(
                        id = VirtualAccountOperationId(
                            transactionId = TransactionId(resultSet.getString("TransactionId")),
                            accountId = VirtualAccountId(
                                resultSet.getString("VirtualAccountId"),
                            ),
                        ),
                        amount = resultSet.getLong("Amount"),
                        createdOn = resultSet.getTimestamp("CreatedOn").toInstant(),
                    )
                }
            }
            operations.right()
        }
    }
}