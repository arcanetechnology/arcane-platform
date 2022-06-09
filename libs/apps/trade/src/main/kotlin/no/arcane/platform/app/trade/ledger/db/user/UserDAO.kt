package no.arcane.platform.app.trade.ledger.db.user

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import no.arcane.platform.user.UserId
import no.arcane.platform.app.trade.ledger.User
import no.arcane.platform.app.trade.ledger.db.selectStatement
import no.arcane.platform.app.trade.ledger.db.updateStatement
import no.arcane.platform.app.trade.ledger.db.usingConnection
import java.time.Instant
import javax.sql.DataSource

class UserDAO(
    private val dataSource: DataSource,
) {
    suspend fun createUser(
        userId: UserId,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            updateStatement(
                """
                insert into users(
                    user_id,
                    created_on
                ) values (
                    ?, 
                    now()
                );
                """.trimIndent(),
            ) {
                setString(1, userId.value)
            }
        }
    }

    suspend fun fetchUser(
        userId: UserId,
    ): Either<String, User> {
        return dataSource.usingConnection {
            either {
                val resultSet = selectStatement(
                    """
                    select * 
                        from users 
                    where user_id = ?;
                   """.trimIndent()
                ) {
                    setString(1, userId.value)
                }.bind()
                if (resultSet.next()) {
                    User(
                        userId = UserId(resultSet.getString("user_id")),
                        createdOn = Instant.ofEpochMilli(resultSet.getTimestamp("created_on").time)
                    ).right()
                } else {
                    "User not found".left()
                }.bind()
            }
        }
    }
}