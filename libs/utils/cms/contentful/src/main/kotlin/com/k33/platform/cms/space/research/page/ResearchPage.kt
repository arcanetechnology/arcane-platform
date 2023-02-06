package com.k33.platform.cms.space.research.page

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import com.k33.platform.cms.clients.ContentfulGraphql
import com.k33.platform.cms.content.Content
import com.k33.platform.cms.sync.Algolia
import com.k33.platform.cms.utils.optional
import com.k33.platform.cms.utils.richToPlainText
import com.k33.platform.utils.config.lazyResourceWithoutWhitespace

class ResearchPage(
    spaceId: String,
    token: String,
) : Content {
    private val client by lazy {
        ContentfulGraphql(
            spaceId = spaceId,
            token = token,
            type = "page"
        ) {
            Algolia.Key.ObjectID *= "sys.id"
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

    private val queryOne by lazyResourceWithoutWhitespace("/research/page/queryOne.graphql")

    override suspend fun fetch(entityId: String): JsonObject? {
        val ids = fetchIdToModifiedMap().keys
        return client.fetch(queryOne, "pageId" to entityId)
            .singleOrNull()
            ?.let { jsonObject ->
                if (ids.contains(jsonObject.objectIDString())) {
                    jsonObject
                } else {
                    null
                }
            }
    }

    private val queryMany by lazyResourceWithoutWhitespace("/research/page/queryMany.graphql")

    override suspend fun fetchAll(): Collection<JsonObject> {
        val ids = fetchIdToModifiedMap().keys
        return client.fetch(queryMany)
            .filter { ids.contains(it.objectIDString()) }
    }

    private val clientForIds by lazy {

        ContentfulGraphql(
            spaceId = spaceId,
            token = token,
            type = "pageWeeklyUpdate"
        ) {
            Algolia.Key.ObjectID *= "linkedFrom.pageCollection.items[*].sys.id"
            "publishedAt" *= "linkedFrom.pageCollection.items[*].sys.publishedAt"
        }
    }

    private val queryIds by lazyResourceWithoutWhitespace("/research/page/article/queryIds.graphql")

    override suspend fun fetchIdToModifiedMap(): Map<String, String> {
        return clientForIds
            .fetch(queryIds)
            .mapNotNull {
                (it[Algolia.Key.ObjectID]?.jsonArray?.getOrNull(0)?.jsonPrimitive?.content ?: return@mapNotNull null) to
                        (it["publishedAt"]?.jsonArray?.getOrNull(0)?.jsonPrimitive?.content ?: return@mapNotNull null)
            }
            .toMap()
    }

    private fun JsonObject.objectIDString(): String = getValue(Algolia.Key.ObjectID).jsonPrimitive.content
}