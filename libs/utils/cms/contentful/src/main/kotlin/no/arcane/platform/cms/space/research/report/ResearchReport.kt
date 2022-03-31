package no.arcane.platform.cms.space.research.report

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import no.arcane.platform.cms.clients.ContentfulGraphql
import no.arcane.platform.cms.content.Content
import no.arcane.platform.cms.utils.richToPlainText
import no.arcane.platform.utils.config.lazyResourceWithoutWhitespace

class ResearchReport(
    spaceId: String,
    token: String,
): Content {

    private val client by lazy {

        ContentfulGraphql(
            spaceId = spaceId,
            token = token,
            type = "report"
        ) {
            "objectID" *= "sys.id"
            "title" *= "title"
            "slug" *= "slug"
            "subtitle" *= "subtitle"
            "description" *= { richToPlainText("description.json") }
            "pdf" *= "pdf"
            "image" *= "image"
            "publishDate" *= "publishDate"
            "tags" *= "tagsCollection.items[*].name"
            "sponsors" *= "sponsorsCollection.items[*]"
            "publishedAt" *= "sys.publishedAt"
        }
    }

    private val queryOne by lazyResourceWithoutWhitespace("/research/report/queryOne.graphql")


    override suspend fun fetch(entityId: String): JsonObject? {
        return client.fetch(queryOne, "reportId" to entityId).singleOrNull()
    }

    private val queryMany by lazyResourceWithoutWhitespace("/research/report/queryMany.graphql")

    override suspend fun fetchAll(): Collection<JsonObject> = client.fetch(queryMany)

    private val clientForIds by lazy {

        ContentfulGraphql(
            spaceId = spaceId,
            token = token,
            type = "report"
        ) {
            "objectID" *= "sys.id"
            "publishedAt" *= "sys.publishedAt"
        }
    }

    private val queryIds by lazyResourceWithoutWhitespace("/research/report/queryIds.graphql")

    override suspend fun fetchIdToModifiedMap(): Map<String, String> {
        return clientForIds
            .fetch(queryIds)
            .mapNotNull {
                (it["objectID"]?.jsonPrimitive?.content ?: return@mapNotNull null) to
                        (it["publishedAt"]?.jsonPrimitive?.content ?: return@mapNotNull null)
            }
            .toMap()
    }
}