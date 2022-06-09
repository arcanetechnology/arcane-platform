package no.arcane.platform.app.trade.ledger.db.spanner

import com.google.cloud.Timestamp
import java.time.Instant
import java.util.*

fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(this.seconds, this.nanos.toLong())

fun String.toUUID(): UUID = UUID.fromString(this)