package no.arcane.platform.apps.trade.test

import com.google.cloud.spanner.DatabaseId
import com.google.cloud.spanner.InstanceConfigId
import com.google.cloud.spanner.InstanceId
import com.google.cloud.spanner.InstanceInfo
import com.google.cloud.spanner.Spanner
import com.google.cloud.spanner.SpannerOptions
import com.google.cloud.spanner.Statement
import no.arcane.platform.utils.config.readResourceLines
import java.io.File

fun setupSpannerWithTestData(ddlFile: File) {
    if (System.getenv("SPANNER_EMULATOR_HOST") == null) {
        throw Exception("SPANNER_EMULATOR_HOST env variable is not set.")
    }
    if (System.getenv("GOOGLE_CLOUD_PROJECT") == null) {
        throw Exception("GOOGLE_CLOUD_PROJECT env variable is not set.")
    }

    val instanceId = InstanceId.of(System.getenv("GOOGLE_CLOUD_PROJECT"), "test")

    fun createInstance(spanner: Spanner) {
        val instanceConfig = InstanceConfigId.of(System.getenv("GOOGLE_CLOUD_PROJECT"), "emulator-config")
        val adminClient = spanner.instanceAdminClient
        adminClient.createInstance(
            InstanceInfo
                .newBuilder(instanceId)
                .setNodeCount(1)
                .setDisplayName("test")
                .setInstanceConfigId(instanceConfig)
                .build()
        ).get()
    }

    fun createDatabase(spanner: Spanner) {
        spanner.databaseAdminClient
            .createDatabase(
                "test",
                "test",
                readSqlFileLines(ddlFile),
            )
            .get();
    }

    fun populateTestData(spanner: Spanner) {
        val databaseId = DatabaseId.of(instanceId, "test")
        val client = spanner.getDatabaseClient(databaseId)
        SqlResourceReader
            .readSqlResourceFileLines("/spanner.sql")
            .forEach { sql ->
                client
                    .readWriteTransaction()
                    .run { transaction ->
                        transaction.executeUpdate(Statement.of(sql))
                    }
            }
    }

    val options: SpannerOptions = SpannerOptions
        .newBuilder()
        .build()
    options.service.use { spanner ->
        try {
            createInstance(spanner)
            createDatabase(spanner)
            populateTestData(spanner)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private object SqlResourceReader {
    fun readSqlResourceFileLines(
        resourceFile: String
    ): List<String> = readResourceLines(resourceFile).join()
}

private fun readSqlFileLines(file: File): List<String> = file
    .readLines()
    .join()

private fun List<String>.join(): List<String> {

    data class Lines(
        val lines: List<String> = emptyList(),
        val incompleteLine: String = "",
    ) {
        fun newline(): Lines {
            return if (incompleteLine.isBlank()) {
                this
            } else {
                Lines(
                    this.lines + this.incompleteLine,
                    ""
                )
            }
        }

        fun append(string: String): Lines {
            return if (incompleteLine.isBlank()) {
                this.copy(incompleteLine = string)
            } else {
                this.copy(incompleteLine = this.incompleteLine + " " + string)
            }
        }
    }

    return fold(Lines()) { lines, line ->
        if (line.isBlank()) {
            lines.newline()
        } else {
            lines.append(line)
        }
    }
        .newline()
        .lines
        .map {
            it
                .replace(Regex("\\s+"), " ")
                .removeSuffix(";")
        }
}