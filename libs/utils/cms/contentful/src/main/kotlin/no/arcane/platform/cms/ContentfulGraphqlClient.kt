package no.arcane.platform.cms

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger

object ContentfulGraphqlClient {

    private val logger by getLogger()

    private val client by lazy {

        val contentfulConfig = loadConfig<ContentfulConfig>("contentful", "contentful").value

        HttpClient(CIO) {
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
                url("https://graphql.contentful.com/content/v1/spaces/${contentfulConfig.spaceId}")
                header("Authorization", "Bearer ${contentfulConfig.token}")
                contentType(ContentType.Application.Json)
            }
        }
    }

    suspend fun fetchLegalEntryMetadata(id: String): LegalEntryMetadata? {

        val response: GraphqlResponse = client.post {
            body = GraphqlRequest(
                query = """query { legalTextCollection (where: { titleOfLegalText: "$id" }, limit:10) { items { titleOfLegalText sys { id spaceId environmentId publishedVersion } } } }"""
            )
        }

        if (response.errors != null) {
            logger.error("${response.errors}")
            return null
        }
        val legalText = response.data?.legalTextCollection?.items?.singleOrNull() ?: return null

        return LegalEntryMetadata(
            id = legalText.titleOfLegalText,
            version = legalText.sys.publishedVersion,
            spaceId = legalText.sys.spaceId,
            environmentId = legalText.sys.environmentId,
            entryId = legalText.sys.id,
        )
    }

    @Serializable
    private data class GraphqlRequest(
        val query: String
    )

    @Serializable
    private data class GraphqlResponse(
        val data: Data? = null,
        val errors: List<String>? = null,
    )

    @Serializable
    private data class Data(
        val legalTextCollection: LegalTextCollection
    )

    @Serializable
    private data class LegalTextCollection(
        val items: List<LegalText>
    )

    @Serializable
    private data class LegalText(
        val titleOfLegalText: String,
        val sys: Sys
    )

    @Serializable
    private data class Sys(
        val id: String,
        val spaceId: String,
        val environmentId: String,
        val publishedVersion: String,
    )
}