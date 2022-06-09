package no.arcane.platform.app.trade.ledger.db.spanner.profile

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import com.google.cloud.Timestamp
import com.google.cloud.spanner.Mutation
import com.google.cloud.spanner.ResultSet
import com.google.cloud.spanner.Statement
import no.arcane.platform.app.trade.ledger.db.spanner.Profile
import no.arcane.platform.app.trade.ledger.db.spanner.ProfileId
import no.arcane.platform.app.trade.ledger.db.spanner.ProfileType
import no.arcane.platform.app.trade.ledger.db.spanner.Table
import no.arcane.platform.app.trade.ledger.db.spanner.readWriteTransaction
import no.arcane.platform.app.trade.ledger.db.spanner.toInstant
import no.arcane.platform.app.trade.ledger.db.spanner.updateMutation
import no.arcane.platform.user.UserId

object ProfileStore {

    private val ProfilesTable = object : Table<Profile> {
        override val name: String = "Profiles"
        override val columns: List<String> = listOf("UserId", "ProfileId", "Alias", "Type", "CreatedOn", "UpdatedOn")
        override fun ResultSet.toObject() = Profile(
            profileId = ProfileId(
                userId = getString("UserId"),
                value = getString("ProfileId"),
            ),
            alias = getString("Alias"),
            type = ProfileType.valueOf(getString("Type")),
            createdOn = getTimestamp("CreatedOn").toInstant(),
            updatedOn = getTimestamp("UpdatedOn").toInstant(),
        )
    }

    private fun ProfileType.maxLimit() = when (this) {
        ProfileType.PERSONAL -> 1
        ProfileType.BUSINESS -> 5
    }

    suspend fun add(
        profileId: ProfileId,
        alias: String,
        type: ProfileType,
    ): Either<String, Unit> {
        return readWriteTransaction { txn ->
            either {
                val profiles = mutableListOf<Profile>()
                txn

                    .executeQuery(
                        Statement.newBuilder(
                            """
                            SELECT *
                            FROM Profiles
                            WHERE UserId = @userId
                            AND Type = @type
                            """.trimIndent()
                        )
                            .bind("userId").to(profileId.userId)
                            .bind("type").to(type.name)
                            .build()
                    )
                    .use { resultSet ->
                        while (resultSet.next()) {
                            profiles += resultSet.toProfile()
                        }
                    }

                if (profiles.size >= type.maxLimit()) {
                    "Max limit reached for ${type.name} profiles".left().bind<Unit>()
                }
                val rowsCreated = txn.executeUpdate(
                        Statement.newBuilder(
                            """
                            INSERT Profiles (UserId, ProfileId, Alias, Type, CreatedOn, UpdatedOn)
                            VALUES (@userId, @profileId, @alias, @type, @now, @now)
                            """.trimIndent()
                        )
                            .bind("userId").to(profileId.userId)
                            .bind("profileId").to(profileId.value)
                            .bind("alias").to(alias)
                            .bind("type").to(type.name)
                            .bind("now").to(Timestamp.now())
                            .build()
                    )
                if (rowsCreated != 1L) {
                    "Profile not created".left().bind<Unit>()
                }
            }
        }
    }

    suspend fun update(
        profileId: ProfileId,
        alias: String,
    ): Either<String, Unit> {
        return updateMutation(
            Mutation.newUpdateBuilder("Profiles")
                .set("UserId").to(profileId.userId)
                .set("ProfileId").to(profileId.value)
                .set("Alias").to(alias)
                .set("UpdatedOn").to(Timestamp.now())
                .build(),
            error = "Update profile failed",
        )
    }

    suspend fun get(
        profileId: ProfileId,
    ): Either<String, Profile> = ProfilesTable.get(profileId.userId, profileId.value)

    suspend fun getAll(
        userId: UserId,
    ): Either<String, List<Profile>> = ProfilesTable.getAll(userId.value)

    private fun ResultSet.toProfile() = Profile(
        profileId = ProfileId(
            userId = getString("UserId"),
            value = getString("ProfileId"),
        ),
        alias = getString("Alias"),
        type = ProfileType.valueOf(getString("Type")),
        createdOn = getTimestamp("CreatedOn").toInstant(),
        updatedOn = getTimestamp("UpdatedOn").toInstant(),
    )
}