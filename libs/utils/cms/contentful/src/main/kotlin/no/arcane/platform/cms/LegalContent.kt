package no.arcane.platform.cms

import kotlinx.serialization.json.jsonPrimitive
import no.arcane.platform.cms.clients.ContentfulClient
import no.arcane.platform.cms.clients.ContentfulGraphql
import no.arcane.platform.utils.config.lazyResourceWithoutWhitespace
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger

object LegalContent {

    private val logger by getLogger()

    internal val contentfulConfig by loadConfig<ContentfulConfig>("contentful", "contentful")

    private val graphqlClient by lazy {
        ContentfulGraphql(
            spaceId = contentfulConfig.spaceId,
            token = contentfulConfig.token,
            type = "legalText"
        ) {
            "titleOfLegalText" *= "titleOfLegalText"
            "publishedVersion" *= "sys.publishedVersion"
            "spaceId" *= "sys.spaceId"
            "environmentId" *= "sys.environmentId"
            "entryId" *= "sys.id"
        }
    }

    private val queryOne by lazyResourceWithoutWhitespace("/legal/legalText/queryOne.graphql")

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
        val jsonObject = graphqlClient.fetch(queryOne, "id" to id).singleOrNull() ?: return null
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