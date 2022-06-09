package no.arcane.platform.app.trade.ledger.db.profile

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import no.arcane.platform.user.UserId
import no.arcane.platform.app.trade.ledger.Profile
import no.arcane.platform.app.trade.ledger.ProfileId
import no.arcane.platform.app.trade.ledger.ProfileType
import no.arcane.platform.app.trade.ledger.db.selectStatement
import no.arcane.platform.app.trade.ledger.db.updateStatement
import no.arcane.platform.app.trade.ledger.db.usingConnection
import java.sql.ResultSet
import java.sql.Types
import java.time.Instant
import java.util.*
import javax.sql.DataSource

class ProfileDAO(
    private val dataSource: DataSource
) {
    suspend fun addProfile(
        userId: UserId,
        profileId: ProfileId,
        alias: String,
        type: ProfileType,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            // TODO restrict personal profiles to 1 and business profiles to 5
            updateStatement(
                """
                insert into profiles(
                    profile_id, 
                    alias,
                    type, 
                    user_id,
                    created_on, 
                    updated_on 
                ) VALUES (
                    ?, 
                    ?, 
                    ?, 
                    ?, 
                    now(), 
                    now()
                );
                """.trimIndent()
            ) {
                setObject(1, profileId.value)
                setString(2, alias)
                setObject(3, type, Types.OTHER)
                setString(4, userId.value)
            }
        }
    }

    suspend fun updateProfile(
        userId: UserId,
        profileId: ProfileId,
        alias: String,
    ): Either<String, Unit> {
        return dataSource.usingConnection {
            updateStatement(
                """
                update profiles
                    set alias = ?
                where user_id = ? 
                    and profile_id = ?;
                """.trimIndent()
            ) {
                setString(1, alias)
                setString(2, userId.value)
                setObject(3, profileId.value)
            }
        }
    }

    suspend fun fetchProfile(
        userId: UserId,
        profileId: ProfileId,
    ): Either<String, Profile> {
        return dataSource.usingConnection {
            either {
                val resultSet = selectStatement(
                    """
                    select *
                        from profiles
                    where user_id = ?
                        and profile_id = ?
                    """.trimIndent()
                ) {
                    setString(1, userId.value)
                    setObject(2, profileId.value)
                }.bind()
                if (resultSet.next()) {
                    resultSet.toProfile().right()
                } else {
                    "Profile not found".left()
                }.bind()
            }
        }
    }

    suspend fun fetchProfiles(
        userId: UserId,
    ): Either<String, List<Profile>> {
        return dataSource.usingConnection {
            either {
                val resultSet = selectStatement(
                    """
                    select *
                        from profiles
                    where user_id = ?
                    """.trimIndent()
                ) {
                    setString(1, userId.value)
                }.bind()
                if (!resultSet.next()) {
                    "Profiles not found".left()
                } else {
                    val profiles = mutableListOf<Profile>()
                    do {
                        profiles += resultSet.toProfile()
                    } while (resultSet.next())
                    profiles.right()
                }.bind()
            }
        }
    }

    private fun ResultSet.toProfile() = Profile(
        profileId = ProfileId(getObject("profile_id") as UUID),
        alias = getString("alias"),
        type = ProfileType.valueOf(getString("type")),
        createdOn = Instant.ofEpochMilli(getTimestamp("created_on").time),
        updatedOn = Instant.ofEpochMilli(getTimestamp("updated_on").time),
    )
}