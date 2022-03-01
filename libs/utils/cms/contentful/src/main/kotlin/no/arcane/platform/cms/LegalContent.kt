package no.arcane.platform.cms

import kotlinx.serialization.json.jsonPrimitive
import no.arcane.platform.cms.clients.ContentfulClient
import no.arcane.platform.cms.clients.ContentfulGraphql
import no.arcane.platform.cms.clients.text
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.config.readResource
import no.arcane.platform.utils.logging.getLogger

object LegalContent {

    private val logger by getLogger()

    internal val contentfulConfig by loadConfig<ContentfulConfig>("contentful", "contentful")

    private val graphqlClient by lazy {
        ContentfulGraphql(
            spaceId = contentfulConfig.spaceId,
            token = contentfulConfig.token,
            query = readResource("/legal/query.graphql").replace(Regex("\\s+"), " "),
        ).SimpleClient(
            transformations = mapOf(
                "titleOfLegalText" to "$.data.legalTextCollection.items[*].titleOfLegalText".text(),
                "publishedVersion" to "$.data.legalTextCollection.items[*].sys.publishedVersion".text(),
                "spaceId" to "$.data.legalTextCollection.items[*].sys.spaceId".text(),
                "environmentId" to "$.data.legalTextCollection.items[*].sys.environmentId".text(),
                "entryId" to "$.data.legalTextCollection.items[*].sys.id".text(),
            )
        )
    }

    private val client by lazy {
        ContentfulClient(
            contentfulConfig.spaceId,
            contentfulConfig.token,
        )
    }

    fun checkSpaceId(spaceId: String): Boolean {
        val found = spaceId == contentfulConfig.spaceId
        if (!found) {
            logger.error("spaceId not found: $spaceId")
        }
        return found
    }

    suspend fun fetchLegalEntryMetadata(id: String): LegalEntryMetadata? {
        val jsonObject = graphqlClient.fetch("id" to id).singleOrNull() ?: return null
        return LegalEntryMetadata(
            id = jsonObject["titleOfLegalText"]!!.jsonPrimitive.content,
            version = jsonObject["publishedVersion"]!!.jsonPrimitive.content,
            spaceId = jsonObject["spaceId"]!!.jsonPrimitive.content,
            environmentId = jsonObject["environmentId"]!!.jsonPrimitive.content,
            entryId = jsonObject["entryId"]!!.jsonPrimitive.content,
        )
    }

    suspend fun getRichTextAsHtml(
        id: String,
    ): String? {
        val metadata = fetchLegalEntryMetadata(id)
        if (metadata == null) {
            logger.error("cms entry not found: $id")
            return null
        }
        return client.getRichTextAsHtml(
            entryId = metadata.entryId,
            fieldId = metadata.fieldId,
        )
    }
}