package no.arcane.platform.app.trade.ledger.db.account

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import no.arcane.platform.app.trade.ledger.CryptoCurrency
import no.arcane.platform.app.trade.ledger.CryptoCustodyAccount
import no.arcane.platform.app.trade.ledger.CryptoCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.selectStatement
import no.arcane.platform.app.trade.ledger.db.updateStatement
import no.arcane.platform.app.trade.ledger.db.usingConnection
import java.time.Instant
import java.util.*
import javax.sql.DataSource

class CryptoCustodyAccountDAO(
    private val dataSource: DataSource
) {
    suspend fun addCryptoCustodyAccount(
        cryptoCustodyAccountId: CryptoCustodyAccountId,
        cryptoCurrency: CryptoCurrency,
        alias: String,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            updateStatement(
                """
                insert into crypto_custody_accounts(
                    crypto_custody_account_id, 
                    balance, 
                    reserved_balance, 
                    crypto_currency, 
                    alias,
                    created_on, 
                    updated_on
                ) values (
                    ?,
                    0,
                    0,
                    ?,
                    ?,
                    now(),
                    now()
                );
                """.trimIndent()
            ) {
                setObject(1, cryptoCustodyAccountId.value)
                setString(2, cryptoCurrency.value)
                setString(3, alias)
            }
        }
    }

    suspend fun updateCryptoCustodyAccount(
        cryptoCustodyAccountId: CryptoCustodyAccountId,
        alias: String,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            updateStatement(
                """
                update crypto_custody_accounts
                    set alias = ?,
                    updated_on = now()
                where crypto_custody_account_id = ?;
                """.trimIndent()
            ) {
                setString(1, alias)
                setObject(2, cryptoCustodyAccountId.value)
            }
        }
    }

    suspend fun fetchCryptoCustodyAccount(
        cryptoCurrency: CryptoCurrency,
    ): Either<String, CryptoCustodyAccount> {
        return dataSource.usingConnection {
            either {
                val resultSet = selectStatement(
                    """
                    select *
                        from crypto_custody_accounts
                    where
                        crypto_currency = ?;
                    """.trimIndent()
                ) {
                    setString(1, cryptoCurrency.value)
                }.bind()
                if (resultSet.next()) {
                    CryptoCustodyAccount(
                        cryptoCustodyAccountId = CryptoCustodyAccountId(resultSet.getObject("crypto_custody_account_id") as UUID),
                        balance = resultSet.getBigDecimal("balance").toBigInteger(),
                        reservedBalance = resultSet.getBigDecimal("reserved_balance").toBigInteger(),
                        cryptoCurrency = CryptoCurrency(resultSet.getString("crypto_currency")),
                        alias = resultSet.getString("alias"),
                        createdOn = Instant.ofEpochMilli(resultSet.getTimestamp("created_on").time),
                        updatedOn = Instant.ofEpochMilli(resultSet.getTimestamp("updated_on").time),
                    ).right()
                } else {
                    "CryptoCustodyAccount not found".left()
                }.bind()
            }
        }
    }
}