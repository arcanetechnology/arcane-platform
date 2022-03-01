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
import net.andreinc.mapneat.dsl.json
import no.arcane.platform.utils.logging.getLogger
import java.util.*

class ContentfulGraphqlClient(
    spaceId: String,
    token: String,
    private val query: String,
    private val transformations: Map<String, Transformation>
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

    suspend fun fetch(vararg variables: Pair<String, Any>): List<JsonObject> {

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
            return emptyList()
        }

        fun getPlainText(map: LinkedHashMap<String, Any>): String {
            return if (map["nodeType"] == "text") {
                map["value"] as? String
            } else {
                val list = map["content"] as? List<LinkedHashMap<String, Any>>
                list?.joinToString(separator = "") {
                    getPlainText(it)
                }
            } ?: ""
        }

        val transformed = json(response) {
            transformations.forEach { (key, transformation) ->
                key *= {
                    expression = transformation.expression
                    if (transformation.isRichText) {
                        processor = { richTextMapList ->
                            (richTextMapList as LinkedList<*>)
                                .map { richTextMap ->
                                    getPlainText(richTextMap as LinkedHashMap<String, Any>)
                                }
                        }
                    }
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

data class Transformation(
    val expression: String,
    val isRichText: Boolean = false
)

fun String.text() = Transformation(this)
fun String.richText() = Transformation(this, true)
