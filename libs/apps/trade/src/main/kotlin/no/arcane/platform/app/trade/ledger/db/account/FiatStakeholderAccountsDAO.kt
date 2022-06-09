package no.arcane.platform.app.trade.ledger.db.account

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import no.arcane.platform.user.UserId
import no.arcane.platform.app.trade.ledger.Currency
import no.arcane.platform.app.trade.ledger.FiatCustodyAccountId
import no.arcane.platform.app.trade.ledger.FiatStakeholderAccount
import no.arcane.platform.app.trade.ledger.FiatStakeholderAccountId
import no.arcane.platform.app.trade.ledger.ProfileId
import no.arcane.platform.app.trade.ledger.db.selectStatement
import no.arcane.platform.app.trade.ledger.db.updateStatement
import no.arcane.platform.app.trade.ledger.db.usingConnection
import no.arcane.platform.app.trade.ledger.db.validate
import java.sql.ResultSet
import java.sql.Types
import java.time.Instant
import java.util.*
import javax.sql.DataSource

class FiatStakeholderAccountsDAO(
    private val dataSource: DataSource
) {
    suspend fun addFiatStakeholderAccount(
        userId: UserId,
        profileId: ProfileId,
        fiatCustodyAccountId: FiatCustodyAccountId,
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        currency: Currency,
        alias: String,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            either {
                validate(
                    """
                    select *
                        from profiles
                    where profile_id = ?
                        and user_id = ?
                    """.trimIndent(), error = "Profile not found"
                ) {
                    setObject(1, profileId.value)
                    setString(2, userId.value)
                }.bind()
                updateStatement(
                    """
                insert into fiat_stakeholder_accounts(
                    fiat_stakeholder_account_id, 
                    balance, 
                    reserved_balance, 
                    currency, 
                    alias,
                    profile_id,
                    fiat_custody_account_id,
                    created_on, 
                    updated_on
                ) values (
                    ?,
                    0,
                    0,
                    ?,
                    ?,
                    ?,
                    ?,
                    now(),
                    now()
                );
                """.trimIndent()
                ) {
                    setObject(1, fiatStakeholderAccountId.value)
                    setObject(2, currency, Types.OTHER)
                    setString(3, alias)
                    setObject(4, profileId.value)
                    setObject(5, fiatCustodyAccountId.value)
                }
            }
        }
    }

    suspend fun updateFiatStakeholderAccount(
        userId: UserId,
        profileId: ProfileId,
        fiatStakeholderAccountId: FiatStakeholderAccountId,
        alias: String,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            either {
                validate(
                    """
                    select *
                        from profiles
                    where profile_id = ?
                        and user_id = ?
                    """.trimIndent(), error = "Profile not found"
                ) {
                    setObject(1, profileId.value)
                    setString(2, userId.value)
                }.bind()
                updateStatement(
                    """
                update fiat_stakeholder_accounts
                    set alias = ?,
                    updated_on = now()
                where fiat_stakeholder_account_id = ?;
                """.trimIndent()
                ) {
                    setString(1, alias)
                    setObject(2, fiatStakeholderAccountId.value)
                }
            }
        }
    }

    suspend fun fetchFiatStakeholderAccount(
        userId: UserId,
        fiatStakeholderAccountId: FiatStakeholderAccountId,
    ): Either<String, FiatStakeholderAccount> {
        return dataSource.usingConnection {
            either {
                val resultSet = selectStatement(
                    """
                select fiat_stakeholder_accounts.* 
                    from fiat_stakeholder_accounts
                    inner join profiles on fiat_stakeholder_accounts.profile_id = profiles.profile_id
                where
                    fiat_stakeholder_account_id = ?
                    and profiles.user_id = ?;
                """.trimIndent()
                ) {
                    setObject(1, fiatStakeholderAccountId.value)
                    setString(2, userId.value)
                }.bind()
                if (resultSet.next()) {
                    resultSet.toAccount().right()
                } else {
                    "Account not found".left()
                }.bind()
            }
        }
    }

    suspend fun fetchFiatStakeholderAccounts(
        userId: UserId,
    ): Either<String, List<FiatStakeholderAccount>> {
        return dataSource.usingConnection {
            either {
                val resultSet = selectStatement(
                    """
                select fiat_stakeholder_accounts.* 
                    from fiat_stakeholder_accounts
                    inner join profiles on fiat_stakeholder_accounts.profile_id = profiles.profile_id
                where
                    profiles.user_id = ?;
                """.trimIndent()
                ) {
                    setString(1, userId.value)
                }.bind()
                if (!resultSet.next()) {
                    val accounts = mutableListOf<FiatStakeholderAccount>()
                    do {
                        accounts += resultSet.toAccount()
                    } while (resultSet.next())
                    accounts.right()
                } else {
                    "Accounts not found".left()
                }.bind()

            }
        }
    }

    private fun ResultSet.toAccount() = FiatStakeholderAccount(
        fiatStakeholderAccountId = FiatStakeholderAccountId(getObject("fiat_stakeholder_account_id") as UUID),
        fiatCustodyAccountId = FiatCustodyAccountId(getObject("fiat_custody_account_id") as UUID),
        balance = getBigDecimal("balance").toBigInteger(),
        reservedBalance = getBigDecimal("reserved_balance").toBigInteger(),
        currency = Currency.valueOf(getString("currency")),
        alias = getString("alias"),
        profileId = ProfileId(getObject("profile_id") as UUID),
        createdOn = Instant.ofEpochMilli(getTimestamp("created_on").time),
        updatedOn = Instant.ofEpochMilli(getTimestamp("updated_on").time),
    )
}