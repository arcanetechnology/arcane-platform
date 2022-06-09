package no.arcane.platform.app.trade.admin.api.rest

import arrow.core.right
import com.google.cloud.spanner.ResultSet
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import no.arcane.platform.app.trade.ledger.db.spanner.toInstant
import java.time.Instant
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

@Serializable
data class AccountOperation(
    val transactionId: String,
    val amount: Long,
    val balance: Long,
    val createdOn: String,
)

fun readAccountOperations(resultSet: ResultSet): List<AccountOperation> {
    val accountOperations = mutableListOf<AccountOperation>()
    while (resultSet.next()) {
        accountOperations += AccountOperation(
            transactionId = resultSet.getString("TransactionId"),
            amount = resultSet.getLong("Amount"),
            balance = resultSet.getLong("Balance"),
            createdOn = resultSet.getTimestamp("CreatedOn").toInstant().toString(),
        )
    }
    return accountOperations
}

fun PipelineContext<Unit, ApplicationCall>.offset(): Instant = try {
    val strOffset = call.request.queryParameters["offset"]
    if (strOffset == null) {
        Instant.now().minus(7, ChronoUnit.DAYS)
    } else {
        Instant.parse(strOffset)
    }
} catch (e: DateTimeParseException) {
    throw BadRequestException("offset is not a valid timestamp")
}

fun PipelineContext<Unit, ApplicationCall>.limit(): ULong = (call.request.queryParameters["limit"] ?: "10")
    .toULongOrNull()
    ?: throw BadRequestException("limit is not an integer")