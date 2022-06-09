package no.arcane.platform.app.trade.ledger.db.account

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import no.arcane.platform.app.trade.ledger.Currency
import no.arcane.platform.app.trade.ledger.FiatCustodyAccount
import no.arcane.platform.app.trade.ledger.FiatCustodyAccountId
import no.arcane.platform.app.trade.ledger.db.selectStatement
import no.arcane.platform.app.trade.ledger.db.updateStatement
import no.arcane.platform.app.trade.ledger.db.usingConnection
import java.sql.Types
import java.time.Instant
import java.util.*
import javax.sql.DataSource

class FiatCustodyAccountsDAO(
    private val dataSource: DataSource
) {
    suspend fun addFiatCustodyAccount(
        fiatCustodyAccountId: FiatCustodyAccountId,
        currency: Currency,
        alias: String,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            updateStatement(
                """
                insert into fiat_custody_accounts(
                    fiat_custody_account_id, 
                    balance, 
                    reserved_balance, 
                    currency, 
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
                setObject(1, fiatCustodyAccountId.value)
                setObject(2, currency, Types.OTHER)
                setString(3, alias)
            }
        }
    }

    suspend fun updateFiatCustodyAccount(
        fiatCustodyAccountId: FiatCustodyAccountId,
        alias: String,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            updateStatement(
                """
                update fiat_custody_accounts
                    set alias = ?,
                    updated_on = now()
                where fiat_custody_account_id = ?;
                """.trimIndent()
            ) {
                setString(1, alias)
                setObject(2, fiatCustodyAccountId.value)
            }
        }
    }

    suspend fun fetchFiatCustodyAccount(
        currency: Currency,
    ): Either<String, FiatCustodyAccount> {
        return dataSource.usingConnection {
            either {
                val resultSet = selectStatement(
                    """
                    select *
                        from fiat_custody_accounts
                    where
                        currency = ?;
                    """.trimIndent()
                ) {
                    setObject(1, currency, Types.OTHER)
                }.bind()
                if (resultSet.next()) {
                    FiatCustodyAccount(
                        fiatCustodyAccountId = FiatCustodyAccountId(resultSet.getObject("fiat_custody_account_id") as UUID),
                        balance = resultSet.getBigDecimal("balance").toBigInteger(),
                        reservedBalance = resultSet.getBigDecimal("reserved_balance").toBigInteger(),
                        currency = Currency.valueOf(resultSet.getString("currency")),
                        alias = resultSet.getString("alias"),
                        createdOn = Instant.ofEpochMilli(resultSet.getTimestamp("created_on").time),
                        updatedOn = Instant.ofEpochMilli(resultSet.getTimestamp("updated_on").time),
                    ).right()
                } else {
                    "FiatCustodyAccount not found".left()
                }.bind()
            }
        }
    }
}