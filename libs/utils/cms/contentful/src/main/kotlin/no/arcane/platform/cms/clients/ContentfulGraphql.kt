package no.arcane.platform.cms.clients

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.andreinc.mapneat.dsl.MapNeat
import net.andreinc.mapneat.dsl.json
import no.arcane.platform.cms.utils.forEachInArrayAt
import no.arcane.platform.cms.utils.getPlainText
import no.arcane.platform.utils.logging.getLogger
import java.util.*

class ContentfulGraphql(
    spaceId: String,
    token: String,
    private val query: String,
) {

    private val logger by getLogger()

    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        defaultRequest {
            url("https://graphql.contentful.com/content/v1/spaces/$spaceId")
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
        }
    }

    private suspend fun fetchResponse(
        vararg variables: Pair<String, Any>
    ): String? {
        val graphqlRequest = buildJsonObject {
            put("query", JsonPrimitive(query))
            put("variables",
                buildJsonObject {
                    variables.forEach { (key, value) ->
                        when (value) {
                            is String -> put(key, JsonPrimitive(value))
                            is Number -> put(key, JsonPrimitive(value))
                            is Boolean -> put(key, JsonPrimitive(value))
                        }
                    }
                }
            )
        }
        val response: String = client.post {
            body = graphqlRequest
        }

        val jsonObject = Json.parseToJsonElement(response).jsonObject
        val errors = jsonObject["errors"]?.jsonArray
        if (!errors.isNullOrEmpty()) {
            logger.error(errors.joinToString())
            return null
        }

        return response
    }

    inner class SimpleClient(
        private val transformations: Map<String, Transformation>
    ) {
        suspend fun fetch(vararg variables: Pair<String, Any>): List<JsonObject> {
            val response: String = fetchResponse(*variables) ?: return emptyList()
            val transformed = json(response) {
                transformations.forEach { (key, transformation) ->
                    if (transformation.isRichText) {
                        key *= {
                            expression = transformation.expression
                            processor = { richTextMapList ->
                                (richTextMapList as LinkedList<*>)
                                    .map { richTextMap ->
                                        getPlainText(richTextMap as LinkedHashMap<String, Any>)
                                    }
                            }
                        }
                    } else {
                        key *= transformation.expression
                    }
                }
            }
            val flatObject = Json.parseToJsonElement(transformed.getString()).jsonObject
            val size = flatObject[flatObject.keys.first()]!!.jsonArray.size
            return (0 until size).map { index ->
                JsonObject(
                    flatObject.keys.associateWith { key ->
                        flatObject[key]!!.jsonArray.getOrNull(index) ?: JsonNull
                    }
                )
            }
        }
    }

    inner class AdvancedClient(
        private val arrayPath: String,
        private val transform: MapNeat.() -> Unit,
    ) {
        suspend fun fetch(vararg variables: Pair<String, Any>): List<JsonObject> {
            val response: String = fetchResponse(*variables) ?: return emptyList()
            val transformed = json(response) {
                "objects" /= forEachInArrayAt(arrayPath, transform)
            }
            return Json.parseToJsonElement(transformed.getString())
                .jsonObject["objects"]!!
                .jsonArray
                .map { it.jsonObject }
        }
    }
}

data class Transformation(
    val expression: String,
    val isRichText: Boolean = false
)

fun String.text() = Transformation(this)
fun String.richText() = Transformation(this, true)

