package no.arcane.platform.app.trade.ledger.db

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import no.arcane.platform.user.UserId
import no.arcane.platform.app.trade.ledger.Amount
import no.arcane.platform.app.trade.ledger.Crypto
import no.arcane.platform.app.trade.ledger.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.CryptoStakeholderAccountId
import no.arcane.platform.app.trade.ledger.Currency
import no.arcane.platform.app.trade.ledger.FiatCustodyAccountId
import no.arcane.platform.app.trade.ledger.FiatStakeholderAccountId
import java.math.BigInteger
import java.sql.Connection
import java.sql.Types
import java.util.*
import javax.sql.DataSource

data class TransactionContext(val txnId: UUID)

class TradeDatastore(
    private val dataSource: DataSource,
) {
    //
    // Fiat Transactions
    //
    suspend fun creditAccount(
        userId: UserId,
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        amount: Amount,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            either {
                if (amount.value.compareTo(BigInteger.ZERO) != 1) {
                    "Non-positive amount value".left().bind<Unit>()
                }

                val fiatCustodyAccountId = getFiatCustodyAccountId(
                    userId = userId,
                    fiatStakeholderAccountId = fiatStakeholderAccountId,
                    currency = amount.currency,
                ).bind()

                transaction {
                    creditBalance(
                        fiatCustodyAccountId = fiatCustodyAccountId,
                        amount = amount,
                    ).bind()

                    creditBalance(
                        fiatStakeholderAccountId = fiatStakeholderAccountId,
                        amount = amount,
                    ).bind()
                }
            }
        }
    }

    suspend fun debitAccount(
        userId: UserId,
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        amount: Amount,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            either {
                if (amount.value.compareTo(BigInteger.ZERO) != 1) {
                    "Non-positive amount value".left().bind<Unit>()
                }

                val fiatCustodyAccountId = getFiatCustodyAccountId(
                    userId = userId,
                    fiatStakeholderAccountId = fiatStakeholderAccountId,
                    currency = amount.currency,
                ).bind()

                transaction {
                    debitBalance(
                        fiatCustodyAccountId = fiatCustodyAccountId,
                        amount = amount,
                    ).bind()

                    debitBalance(
                        fiatStakeholderAccountId = fiatStakeholderAccountId,
                        amount = amount,
                    ).bind()
                }
            }
        }
    }

    //
    // Crypto transactions
    //
    suspend fun creditCryptoAccount(
        userId: UserId,
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
        amount: Amount,
        crypto: Crypto,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            either {
                if (amount.value.compareTo(BigInteger.ZERO) != 1) {
                    "Non-positive amount value".left().bind<Unit>()
                }

                if (crypto.value.compareTo(BigInteger.ZERO) != 1) {
                    "Non-positive crypto value".left().bind<Unit>()
                }

                val fiatStakeholderAccountId = getFiatStakeholderAccountId(
                    userId = userId,
                    cryptoStakeholderAccountId = cryptoStakeholderAccountId,
                ).bind()

                transaction {
                    debitBalance(
                        fiatStakeholderAccountId = fiatStakeholderAccountId,
                        amount = amount,
                    ).bind()

                    creditBalance(
                        cryptoStakeholderAccountId = cryptoStakeholderAccountId,
                        crypto = crypto,
                    )
                }
            }
        }
    }

    suspend fun debitCryptoAccount(
        userId: UserId,
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
        amount: Amount,
        crypto: Crypto,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            either {
                if (amount.value.compareTo(BigInteger.ZERO) != 1) {
                    "Non-positive amount value".left().bind<Unit>()
                }

                if (crypto.value.compareTo(BigInteger.ZERO) != 1) {
                    "Non-positive crypto value".left().bind<Unit>()
                }

                val fiatStakeholderAccountId = getFiatStakeholderAccountId(
                    userId = userId,
                    cryptoStakeholderAccountId = cryptoStakeholderAccountId,
                ).bind()

                transaction {
                    debitBalance(
                        cryptoStakeholderAccountId = cryptoStakeholderAccountId,
                        crypto = crypto,
                    ).bind()
                    creditBalance(
                        fiatStakeholderAccountId = fiatStakeholderAccountId,
                        amount = amount,
                    ).bind()
                }
            }
        }
    }

    //
    // Transactions & Operations
    //

    private suspend fun Connection.transaction(
        txnScope: suspend TransactionContext.() -> Unit
    ): Either<String, Unit> {
        return either {
            val txnId = UUID.randomUUID()
            updateStatement(
                """                        
                insert into transactions(transaction_id, timestamp) VALUES (?, now());
                """.trimIndent()
            ) {
                setObject(1, txnId)
            }.bind()
            TransactionContext(txnId).txnScope()
        }
    }

    private suspend fun Connection.getFiatCustodyAccountId(
        userId: UserId,
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        currency: Currency,
    ): Either<String, FiatCustodyAccountId> {
        return either {
            val resultSet = selectStatement(
                """
                select fiat_custody_account_id
                from fiat_stakeholder_accounts
                where currency = ?
                    and fiat_stakeholder_account_id = ?
                    and profile_id = (select profile_id from profiles where user_id = ?)
                """.trimIndent()
            ) {
                setObject(1, currency, Types.OTHER)
                setObject(2, fiatStakeholderAccountId.value)
                setString(3, userId.value)
            }.bind()

            if (!resultSet.next()) {
                "Failed to find custody account".left().bind<Unit>()
            }

            FiatCustodyAccountId(resultSet.getObject("fiat_custody_account_id") as UUID)
        }
    }

    private suspend fun Connection.getFiatStakeholderAccountId(
        userId: UserId,
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
    ): Either<String, FiatStakeholderAccountId> {
        return either {
            val resultSet = selectStatement(
                """
                    select fiat_stakeholder_accounts.fiat_stakeholder_account_id
                       from crypto_stakeholder_accounts
                       inner join portfolios on crypto_stakeholder_accounts.portfolio_id = portfolios.portfolio_id
                       inner join fiat_stakeholder_accounts on portfolios.fiat_stakeholder_account_id = fiat_stakeholder_accounts.fiat_stakeholder_account_id
                       inner join profiles on fiat_stakeholder_accounts.profile_id = profiles.profile_id
                    where crypto_stakeholder_account_id = ?
                        and user_id = ?
                    """.trimIndent(),
            ) {
                setObject(1, cryptoStakeholderAccountId.value)
                setString(2, userId.value)
            }.bind()

            if (!resultSet.next()) {
                "Failed to find fiat stakeholder account".left().bind<Unit>()
            }

            FiatStakeholderAccountId(resultSet.getObject("fiat_stakeholder_account_id") as UUID)
        }
    }

    //
    // Fiat Custody Account operations
    //

    context(TransactionContext) private suspend fun Connection.creditBalance(
        fiatCustodyAccountId: FiatCustodyAccountId,
        amount: Amount,
    ): Either<String, Unit> {
        return either {
            updateStatement(
                """
                    update fiat_custody_accounts 
                        set balance = balance + ?
                    where
                        currency = ?
                        and fiat_custody_account_id = ?;
                   """.trimIndent()
            ) {
                setObject(1, amount.value)
                setObject(2, amount.currency, Types.OTHER)
                setObject(3, fiatCustodyAccountId.value)
            }.bind()

            updateStatement(
                """
                    insert into fiat_custody_operations(
                        operation_id, 
                        amount,
                        operation_direction,
                        transaction_id, 
                        fiat_custody_account_id
                    ) VALUES (
                        ?,
                        ?,
                        'CREDIT',
                        ?,
                        ?
                    );
                    """.trimIndent()
            ) {
                setObject(1, UUID.randomUUID())
                setObject(2, amount.value)
                setObject(3, txnId)
                setObject(4, fiatCustodyAccountId.value)
            }.bind()
        }
    }

    context(TransactionContext) private suspend fun Connection.debitBalance(
        fiatCustodyAccountId: FiatCustodyAccountId,
        amount: Amount,
    ): Either<String, Unit> {
        return either {
            updateStatement(
                """
                    update fiat_custody_accounts 
                        set balance = balance - ?
                    where
                        balance >= ?
                        and currency = ?
                        and fiat_custody_account_id = ?;
                   """.trimIndent()
            ) {
                setObject(1, amount.value)
                setObject(2, amount.value)
                setObject(3, amount.currency, Types.OTHER)
                setObject(4, fiatCustodyAccountId.value)
            }.bind()

            updateStatement(
                """
                    insert into fiat_custody_operations(
                        operation_id, 
                        amount,
                        operation_direction,
                        transaction_id, 
                        fiat_custody_account_id
                    ) VALUES (
                        ?,
                        ?,
                        'DEBIT',
                        ?,
                        ?
                    );
                    """.trimIndent()
            ) {
                setObject(1, UUID.randomUUID())
                setObject(2, amount.value)
                setObject(3, txnId)
                setObject(4, fiatCustodyAccountId.value)
            }.bind()
        }
    }

    //
    // Fiat Stakeholder Account operations
    //

    context(TransactionContext) private suspend fun Connection.creditBalance(
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        amount: Amount,
    ): Either<String, Unit> {
        return either {
            updateStatement(
                """
                update fiat_stakeholder_accounts 
                    set balance = balance + ?
                where
                    currency = ?
                    and fiat_stakeholder_account_id = ?;
               """.trimIndent()
            ) {
                setObject(1, amount.value)
                setObject(2, amount.currency, Types.OTHER)
                setObject(3, fiatStakeholderAccountId.value)
            }.bind()

            updateStatement(
                """
                insert into fiat_stakeholder_operations(
                    operation_id, 
                    amount,
                    operation_direction,
                    transaction_id, 
                    fiat_stakeholder_account_id
                ) VALUES (
                    ?,
                    ?,
                    'CREDIT',
                    ?,
                    ?
                );
                """.trimIndent()
            ) {
                setObject(1, UUID.randomUUID())
                setObject(2, amount.value)
                setObject(3, txnId)
                setObject(4, fiatStakeholderAccountId.value)
            }.bind()
        }
    }



    context(TransactionContext) private suspend fun Connection.debitBalance(
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        amount: Amount,
    ): Either<String, Unit> {
        return either {
            updateStatement(
                """
                update fiat_stakeholder_accounts 
                    set balance = balance - ?
                where
                    balance >= ?
                    and currency = ?
                    and fiat_stakeholder_account_id = ?;
               """.trimIndent()
            ) {
                setObject(1, amount.value)
                setObject(2, amount.value)
                setObject(3, amount.currency, Types.OTHER)
                setObject(4, fiatStakeholderAccountId.value)
            }.bind()

            updateStatement(
                """
                insert into fiat_stakeholder_operations(
                    operation_id, 
                    amount,
                    operation_direction,
                    transaction_id, 
                    fiat_stakeholder_account_id
                ) VALUES (
                    ?,
                    ?,
                    'DEBIT',
                    ?,
                    ?
                );
                """.trimIndent()
            ) {
                setObject(1, UUID.randomUUID())
                setObject(2, amount.value)
                setObject(3, txnId)
                setObject(4, fiatStakeholderAccountId.value)
            }.bind()
        }
    }

    //
    // Crypto Custody Account operations
    //

    context(TransactionContext) private suspend fun Connection.creditBalance(
        cryptoCustodyAccountId: CryptoCustodyAccountId,
        crypto: Crypto,
    ): Either<String, Unit> {
        return either {
            updateStatement(
                """
                update crypto_custody_accounts 
                    set balance = balance + ?
                where
                    crypto_currency = ?
                    and crypto_custody_account_id = ?;
               """.trimIndent()
            ) {
                setObject(1, crypto.value)
                setString(2, crypto.currency.value)
                setObject(3, cryptoCustodyAccountId.value)
            }.bind()

            updateStatement(
                """
                insert into crypto_custody_operations(
                    operation_id, 
                    amount,
                    operation_direction,
                    transaction_id, 
                    crypto_custody_account_id
                ) VALUES (
                    ?,
                    ?,
                    'CREDIT',
                    ?,
                    ?
                );
                """.trimIndent()
            ) {
                setObject(1, UUID.randomUUID())
                setObject(2, crypto.value)
                setObject(3, txnId)
                setObject(4, cryptoCustodyAccountId.value)
            }.bind()
        }
    }

    context(TransactionContext) private suspend fun Connection.debitBalance(
        cryptoCustodyAccountId: CryptoCustodyAccountId,
        crypto: Crypto,
    ): Either<String, Unit> {
        return either {
            updateStatement(
                """
                update crypto_custody_accounts 
                    set balance = balance - ?
                where
                    balance >= ?
                    and crypto_currency = ?
                    and crypto_custody_account_id = ?;
               """.trimIndent()
            ) {
                setObject(1, crypto.value)
                setObject(2, crypto.value)
                setString(3, crypto.currency.value)
                setObject(4, cryptoCustodyAccountId.value)
            }.bind()

            updateStatement(
                """
                insert into crypto_custody_operations(
                    operation_id, 
                    amount,
                    operation_direction,
                    transaction_id, 
                    crypto_custody_account_id
                ) VALUES (
                    ?,
                    ?,
                    'DEBIT',
                    ?,
                    ?
                );
                """.trimIndent()
            ) {
                setObject(1, UUID.randomUUID())
                setObject(2, crypto.value)
                setObject(3, txnId)
                setObject(4, cryptoCustodyAccountId.value)
            }.bind()
        }
    }

    //
    // Crypto Stakeholder Account operations
    //

    context(TransactionContext) private suspend fun Connection.creditBalance(
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
        crypto: Crypto,
    ): Either<String, Unit> {
        return either {
            updateStatement(
                """
                update crypto_stakeholder_accounts 
                    set balance = balance + ?
                where
                    crypto_currency = ?
                    and crypto_stakeholder_account_id = ?;
               """.trimIndent()
            ) {
                setObject(1, crypto.value)
                setString(2, crypto.currency.value)
                setObject(3, cryptoStakeholderAccountId.value)
            }.bind()

            updateStatement(
                """
                insert into crypto_stakeholder_operations(
                    operation_id, 
                    amount,
                    operation_direction,
                    transaction_id, 
                    crypto_stakeholder_account_id
                ) VALUES (
                    ?,
                    ?,
                    'CREDIT',
                    ?,
                    ?
                );
                """.trimIndent()
            ) {
                setObject(1, UUID.randomUUID())
                setObject(2, crypto.value)
                setObject(3, txnId)
                setObject(4, cryptoStakeholderAccountId.value)
            }.bind()
        }
    }

    context(TransactionContext) private suspend fun Connection.debitBalance(
        cryptoStakeholderAccountId: CryptoStakeholderAccountId,
        crypto: Crypto,
    ): Either<String, Unit> {
        return either {
            updateStatement(
                """
                update crypto_stakeholder_accounts 
                    set balance = balance - ?
                where
                    balance >= ?
                    and crypto_currency = ?
                    and crypto_stakeholder_account_id = ?;
               """.trimIndent()
            ) {
                setObject(1, crypto.value)
                setObject(2, crypto.value)
                setString(3, crypto.currency.value)
                setObject(4, cryptoStakeholderAccountId.value)
            }.bind()

            updateStatement(
                """
                insert into crypto_stakeholder_operations(
                    operation_id, 
                    amount,
                    operation_direction,
                    transaction_id, 
                    crypto_stakeholder_account_id
                ) VALUES (
                    ?,
                    ?,
                    'DEBIT',
                    ?,
                    ?
                );
                """.trimIndent()
            ) {
                setObject(1, UUID.randomUUID())
                setObject(2, crypto.value)
                setObject(3, txnId)
                setObject(4, cryptoStakeholderAccountId.value)
            }.bind()
        }
    }
}