package no.arcane.platform.cms.space.research

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObject

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