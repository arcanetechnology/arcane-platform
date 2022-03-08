package no.arcane.platform.cms.space.research

import kotlinx.serialization.json.JsonObject
import no.arcane.platform.cms.clients.ContentfulGraphql
import no.arcane.platform.cms.utils.optional
import no.arcane.platform.cms.utils.richToPlainText
import no.arcane.platform.utils.config.readResource

class ResearchPage(
    spaceId: String,
    token: String,
) {

    private val client by lazy {

        ContentfulGraphql(
            spaceId = spaceId,
            token = token,
            query = readResource("/research/queryOne.graphql").replace(Regex("\\s+"), " "),
        ).AdvancedClient(
            arrayPath = "data.pageCollection.items"
        ) {
            "objectID" *= "sys.id"
            "title" *= "title"
            "slug" *= "slug"
            "publishedAt" *= "sys.publishedAt"
            optional {
                "subtitle" *= "content.subtitle"
                "image" *= "content.image"
                "tags" *= "content.tagsCollection.items[*].name"
                "authors" *= "content.authorsCollection.items[*]"
                "articleText" *= { richToPlainText("content.content.json") }
                "publishDate" *= "content.publishDate"
            }
        }
    }

    suspend fun fetch(pageId: String): JsonObject? {
        return client.fetch("pageId" to pageId).singleOrNull()
    }
}