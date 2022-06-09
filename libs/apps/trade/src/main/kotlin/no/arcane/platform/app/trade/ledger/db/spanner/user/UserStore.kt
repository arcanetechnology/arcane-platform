package no.arcane.platform.app.trade.ledger.db.spanner.user

import arrow.core.Either
import com.google.cloud.Timestamp
import com.google.cloud.spanner.Mutation
import com.google.cloud.spanner.ResultSet
import no.arcane.platform.app.trade.ledger.db.spanner.Table
import no.arcane.platform.app.trade.ledger.db.spanner.User
import no.arcane.platform.app.trade.ledger.db.spanner.toInstant
import no.arcane.platform.app.trade.ledger.db.spanner.insertMutation
import no.arcane.platform.user.UserId

object UserStore {

    private val UsersTable = object : Table<User> {
        override val name: String = "Users"
        override val columns: List<String> = listOf("UserId", "CreatedOn")
        override fun ResultSet.toObject() = User(
            userId = UserId(getString("UserId")),
            createdOn = getTimestamp("CreatedOn").toInstant(),
        )
    }

    suspend fun add(
        userId: UserId,
    ): Either<String, Unit> {
        return insertMutation(
            Mutation.newInsertBuilder("Users")
                .set("UserId").to(userId.value)
                .set("CreatedOn").to(Timestamp.now())
                .build(),
            error = "Create user failed",
        )
    }

    suspend fun get(
        userId: UserId,
    ): Either<String, User> = UsersTable.get(userId.value)

    suspend fun getAll(): Either<String, List<User>> = UsersTable.getAll()
}