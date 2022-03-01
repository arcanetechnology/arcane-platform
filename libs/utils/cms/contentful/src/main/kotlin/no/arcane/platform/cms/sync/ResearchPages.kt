package no.arcane.platform.cms.sync

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.arcane.platform.cms.clients.ContentfulGraphqlClient
import no.arcane.platform.cms.clients.richText
import no.arcane.platform.cms.clients.text
import no.arcane.platform.utils.config.readResource

private val transformations = mapOf(
    "objectID" to "$.data.pageCollection.items[*].sys.id".text(),
    "title" to "$.data.pageCollection.items[*].title".text(),
    "slug" to "$.data.pageCollection.items[*].slug".text(),
    "subtitle" to "$.data.pageCollection.items[*].content.subtitle".text(),
    "image" to "$.data.pageCollection.items[*].content.image".text(),
    "tags" to "$.data.pageCollection.items[*].content.tagsCollection.items[*].name".text(),
    "authors" to "$.data.pageCollection.items[*].content.authorsCollection.items[*].name".text(),
    "publishDate" to "$.data.pageCollection.items[*].content.publishDate".text(),
    "articleText" to "$.data.pageCollection.items[*].content.content.json".richText(),
    "publishedAt" to "$.data.pageCollection.items[*].sys.publishedAt".text(),
)

class ResearchPage(
    spaceId: String,
    token: String,
) {

    private val client by lazy {

        ContentfulGraphqlClient(
            spaceId,
            token,
            readResource("/research/queryOne.graphql").replace(Regex("\\s+"), " "),
            transformations,
        )
    }

    suspend fun fetch(pageId: String): JsonObject? {
        return client.fetch("pageId" to pageId).singleOrNull()
    }
}

/*class ResearchArticles(
    spaceId: String,
    token: String,
) {

    private val batchClient by lazy {

        ContentfulGraphqlClient(
            spaceId,
            token,
            readResource("/research/queryMany.graphql").replace(Regex("\\s+"), " "),
            transformations,
        )
    }

    suspend fun fetchAll(): Collection<JsonObject> {
        var skip = 0
        val set = mutableSetOf<JsonObject>()
        while (true) {
            val next = batchClient.fetch("skip" to skip, "limit" to 50)
            if (next.isEmpty()) {
                break
            }
            set += next
            skip += 50
        }
        return set
    }
}*/

class ResearchPages(
    spaceId: String,
    token: String,
) {

    private val researchPage by lazy { ResearchPage(spaceId, token) }

    private val researchPagesMetadata by lazy { ResearchPagesMetadata(spaceId, token) }

    suspend fun fetchAll(): Collection<JsonObject> = coroutineScope {
        val flow = flow {
            researchPagesMetadata
                .fetchAll()
                .keys
                .chunked(50) // rate limit is 55
                .forEach {
                    emit(it)
                    delay(1_000)
                }
        }
        val set = mutableSetOf<JsonObject>()
        flow.collect { pageIdList ->
            set += pageIdList.map { pageId ->
                async {
                    researchPage.fetch(pageId)
                }
            }.awaitAll()
                .filterNotNull()
        }
        set
    }
}

class ResearchPagesMetadata(
    spaceId: String,
    token: String,
) {

    private val client by lazy {

        ContentfulGraphqlClient(
            spaceId,
            token,
            readResource("/research/queryIds.graphql").replace(Regex("\\s+"), " "),
            mapOf(
                "objectID" to "$.data.pageCollection.items[*].sys.id".text(),
                "publishedAt" to "$.data.pageCollection.items[*].sys.publishedAt".text(),
            ),
        )
    }

    suspend fun fetchAll(): Map<String, String> {
        return client
            .fetch()
            .mapNotNull {
                (it["objectID"]?.jsonPrimitive?.content ?: return@mapNotNull null) to
                        (it["publishedAt"]?.jsonPrimitive?.content ?: return@mapNotNull null)
            }
            .toMap()
    }
}