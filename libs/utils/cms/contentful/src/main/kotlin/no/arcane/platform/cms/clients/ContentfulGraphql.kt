package no.arcane.platform.cms.clients

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.andreinc.mapneat.dsl.MapNeat
import net.andreinc.mapneat.dsl.json
import no.arcane.platform.cms.utils.forEachInArrayAt
import no.arcane.platform.utils.logging.getLogger

class ContentfulGraphql(
    spaceId: String,
    token: String,
    private val type: String,
    private val transform: MapNeat.() -> Unit,
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
            level = LogLevel.NONE
        }
        defaultRequest {
            url("https://graphql.contentful.com/content/v1/spaces/$spaceId")
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
        }
    }

    private suspend fun fetchResponse(
        query: String,
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
        val response: String = withContext(Dispatchers.IO) {
            client.post {
                body = graphqlRequest
            }
        }

        val jsonObject = Json.parseToJsonElement(response).jsonObject
        val errors = jsonObject["errors"]?.jsonArray
        if (!errors.isNullOrEmpty()) {
            logger.error(errors.joinToString())
            return null
        }

        return response
    }

    suspend fun fetch(
        query: String,
        vararg variables: Pair<String, Any>
    ): List<JsonObject> {
        val response: String = fetchResponse(query, *variables) ?: return emptyList()
        val transformed = json(response) {
            "objects" /= forEachInArrayAt("data.${type}Collection.items", transform)
        }
        return Json.parseToJsonElement(transformed.getString())
            .jsonObject["objects"]!!
            .jsonArray
            .map { it.jsonObject }
    }
}