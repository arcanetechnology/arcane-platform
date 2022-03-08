package no.arcane.platform.cms.space.research

import kotlinx.serialization.json.jsonPrimitive
import no.arcane.platform.cms.clients.ContentfulGraphql
import no.arcane.platform.cms.clients.text
import no.arcane.platform.utils.config.readResource

class ResearchPagesMetadata(
    spaceId: String,
    token: String,
) {

    private val client by lazy {

        ContentfulGraphql(
            spaceId = spaceId,
            token = token,
            query = readResource("/research/queryIds.graphql").replace(Regex("\\s+"), " "),
        ).SimpleClient(
            transformations = mapOf(
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