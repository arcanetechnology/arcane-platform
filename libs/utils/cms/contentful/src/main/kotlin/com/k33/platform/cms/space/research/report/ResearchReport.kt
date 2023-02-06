package com.k33.platform.cms.space.research.report

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.k33.platform.cms.clients.ContentfulGraphql
import com.k33.platform.cms.content.Content
import com.k33.platform.cms.sync.Algolia
import com.k33.platform.cms.utils.richToPlainText
import com.k33.platform.utils.config.lazyResourceWithoutWhitespace

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
            Algolia.Key.ObjectID *= "sys.id"
            "title" *= "title"
            "slug" *= "slug"
            "subtitle" *= "subtitle"
            "description" *= { richToPlainText("description.json") }
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
            Algolia.Key.ObjectID *= "sys.id"
            "publishedAt" *= "sys.publishedAt"
        }
    }

    private val queryIds by lazyResourceWithoutWhitespace("/research/report/queryIds.graphql")

    override suspend fun fetchIdToModifiedMap(): Map<String, String> {
        return clientForIds
            .fetch(queryIds)
            .mapNotNull {
                (it[Algolia.Key.ObjectID]?.jsonPrimitive?.content ?: return@mapNotNull null) to
                        (it["publishedAt"]?.jsonPrimitive?.content ?: return@mapNotNull null)
            }
            .toMap()
    }
}