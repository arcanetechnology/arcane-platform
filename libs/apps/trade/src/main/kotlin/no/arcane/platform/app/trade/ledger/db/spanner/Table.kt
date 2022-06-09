package no.arcane.platform.app.trade.ledger.db.spanner

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.cloud.spanner.Key
import com.google.cloud.spanner.KeySet
import com.google.cloud.spanner.ReadContext
import com.google.cloud.spanner.ResultSet
import com.google.cloud.spanner.Statement

interface Table<E>{

    val name: String
    val columns: List<String>

    fun ResultSet.toObject(): E

    suspend fun getAll(): Either<String, List<E>> {
        return usingSpanner { client ->
            client
                .singleUse()
                .read(
                    name,
                    KeySet.all(),
                    columns,
                )
                .use { resultSet ->
                    val entities = mutableListOf<E>()
                    while (resultSet.next()) {
                        entities += resultSet.toObject()
                    }
                    entities.right()
                }
        }
    }

    suspend fun getAll(vararg keys: String): Either<String, List<E>> {
        if (keys.size > columns.size) {
            return "Out of limit keys passed for $name.getAll()".left()
        }
        val aliasToValueMap = mutableMapOf<String, String>()
        val sql = buildString {
            append("SELECT * FROM $name")
            keys.forEachIndexed { index, key ->
                val column = columns[index]
                val alias = column.replaceFirstChar(Char::lowercaseChar)
                append(if (index == 0) " WHERE " else " AND ")
                append("$column = @$alias")
                aliasToValueMap[alias] = key
            }
        }
        var statementBuilder = Statement.newBuilder(sql)
        aliasToValueMap.forEach { (alias, value) ->
            statementBuilder = statementBuilder.bind(alias).to(value)
        }
        return queryStatement(statementBuilder.build()) { resultSet ->
            val entities = mutableListOf<E>()
            while (resultSet.next()) {
                entities += resultSet.toObject()
            }
            entities.right()
        }
    }

    suspend fun get(vararg keys: String): Either<String, E> {
        return usingSpanner { client ->
            val readContext = client.singleUse()
            get(
                readContext = readContext,
                keys = keys,
            )
        }
    }

    suspend fun get(
        readContext: ReadContext,
        vararg keys: String,
    ): Either<String, E> {
        return readContext.read(
            name,
            KeySet.singleKey(Key.of(*keys)),
            columns,
        ).use { resultSet ->
            if (resultSet.next()) {
                resultSet.toObject().right()
            } else {
                // TODO Use Account ID pattern to represent ID
                "$name(${keys.joinToString(separator = "/")}) not found".left()
            }
        }
    }
}