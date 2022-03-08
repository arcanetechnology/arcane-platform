package no.arcane.platform.cms.space.research

import kotlinx.serialization.json.JsonObject
import no.arcane.platform.cms.clients.ContentfulGraphql
import no.arcane.platform.cms.utils.optional
import no.arcane.platform.cms.utils.richToPlainText
import no.arcane.platform.utils.config.readResource

class ResearchPagesV2(
    spaceId: String,
    token: String,
) {

    private val batchClient by lazy {

        ContentfulGraphql(
            spaceId = spaceId,
            token = token,
            query = readResource("/research/queryMany.graphql").replace(Regex("\\s+"), " "),
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

    suspend fun fetchAll(): Collection<JsonObject> {
        val batchSize = 30
        var skip = 0
        val set = mutableSetOf<JsonObject>()
        while (true) {
            val next = batchClient.fetch("skip" to skip, "limit" to batchSize)
            if (next.isEmpty()) {
                break
            }
            set += next
            skip += batchSize
        }
        return set
    }
}