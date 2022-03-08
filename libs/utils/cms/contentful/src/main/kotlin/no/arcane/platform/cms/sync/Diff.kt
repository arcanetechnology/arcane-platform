package no.arcane.platform.cms.sync

import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import kotlinx.coroutines.runBlocking
import no.arcane.platform.cms.space.research.ResearchPagesMetadata
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger

object Diff {

    private val logger by getLogger()

    private val syncConfig by loadConfig<ContentfulAlgoliaSyncConfig>(
        name = "contentful",
        path = "contentfulAlgoliaSync.researchArticles"
    )

    suspend fun researchArticles() {
        val researchPagesMetadata = ResearchPagesMetadata(
            syncConfig.contentful.spaceId,
            syncConfig.contentful.token
        )
        val pageIdMap = researchPagesMetadata
            .fetchAll()
        logger.info("Found in contentful = (${pageIdMap.size})")

        val algoliaClient = AlgoliaClient(
            ApplicationID(syncConfig.algolia.applicationId),
            APIKey(syncConfig.algolia.apiKey),
            IndexName("articles"),
        )

        val indices = algoliaClient.getAllIds()
        logger.info("Found in algolia = (${indices.size})")

        val newInContentful = pageIdMap.keys - indices.keys
        newInContentful.logAsErrorIfNotEmpty("New in contentful = (${newInContentful.size})")

        val onlyInAlgolia = indices.keys - pageIdMap.keys
        onlyInAlgolia.logAsErrorIfNotEmpty("Only in algolia = (${onlyInAlgolia.size})")

        val common = pageIdMap.keys.intersect(indices.keys)
        val updatedInContentful = common.filter { id ->
            pageIdMap[id]!! > indices[id]!!
        }
        updatedInContentful.logAsErrorIfNotEmpty("Updated in contentful = (${updatedInContentful.size})")

        val upsertIds = newInContentful + updatedInContentful
        logger.info("Upsert id list (${upsertIds.size}) = $upsertIds")
        logger.info("Delete id list (${onlyInAlgolia.size}) = $onlyInAlgolia")
    }

    private fun <E> Collection<E>.logAsErrorIfNotEmpty(message: String) {
        if (this.isEmpty()) {
            logger.info(message)
        } else {
            logger.error(message)
        }
    }
}

fun main() {
    runBlocking {
        Diff.researchArticles()
    }
}