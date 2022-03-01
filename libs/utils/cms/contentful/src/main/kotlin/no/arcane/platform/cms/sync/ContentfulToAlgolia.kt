package no.arcane.platform.cms.sync

import com.algolia.search.helper.toObjectID
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.ObjectID
import com.algolia.search.serialize.KeyObjectID
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger

class ContentfulToAlgolia(
    private val syncId: String
) {

    private val logger by getLogger()

    private val syncConfig by loadConfig<ContentfulAlgoliaSyncConfig>(
        "contentful",
        "contentfulAlgoliaSync.$syncId"
    )

    private val algoliaClient by lazy {
        AlgoliaClient(
            ApplicationID(syncConfig.algolia.applicationId),
            APIKey(syncConfig.algolia.apiKey),
            IndexName("articles")
        )
    }

    private val researchPage by lazy {
        ResearchPage(
            spaceId = syncConfig.contentful.spaceId,
            token = syncConfig.contentful.token,
        )
    }

    private val researchPages by lazy {
        ResearchPagesV2(
            spaceId = syncConfig.contentful.spaceId,
            token = syncConfig.contentful.token,
        )
    }

    suspend fun upsert(entryId: String) {
        val record = researchPage.fetch(pageId = entryId) ?: return
        logger.warn("Exporting record with objectID: ${record.objectID} to algolia")
        algoliaClient.upsert(
            objectID = record.objectID,
            record = record
        )
    }

    suspend fun upsertAll() {
        val records = researchPages
            .fetchAll()
            .map {
                it.objectID to it
            }
        logger.info("Exporting ${records.size} records from contentful to algolia for syncId: $syncId")
        algoliaClient.batchUpsert(records)
    }

    suspend fun delete(entryId: String) {
        val objectID = ObjectID(entryId)
        logger.warn("Deleting objectID: $objectID from algolia")
        algoliaClient.delete(objectID)
    }

    private val JsonObject.objectID get() = getValue(KeyObjectID).jsonPrimitive.content.toObjectID()
}

fun main() {
    runBlocking {
//        ContentfulToAlgolia("researchArticles").upsertAll()
//        ContentfulToAlgolia("researchArticles").upsert("")
//        ContentfulToAlgolia("researchArticles").delete("")
    }
}