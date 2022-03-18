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
import no.arcane.platform.cms.content.ContentFactory
import no.arcane.platform.cms.events.Action
import no.arcane.platform.cms.events.EventHub
import no.arcane.platform.cms.events.EventPattern
import no.arcane.platform.cms.events.EventType
import no.arcane.platform.cms.events.Resource
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger

class ContentfulToAlgolia(
    private val syncId: String
) {

    private val logger by getLogger()

    private val algoliaConfig by loadConfig<AlgoliaConfig>(
        "contentful",
        "contentfulAlgoliaSync.$syncId.algolia"
    )

    private val algoliaClient by lazy {
        with(algoliaConfig) {
            AlgoliaClient(
                ApplicationID(applicationId),
                APIKey(apiKey),
                IndexName(indexName)
            )
        }
    }

    private val content by lazy { ContentFactory.getContent(syncId) }

    suspend fun upsert(entryId: String) {
        val record = content.fetch(entityId = entryId) ?: return
        logger.info("Exporting record with objectID: ${record.objectID} to algolia")
        algoliaClient.upsert(
            objectID = record.objectID,
            record = record
        )
    }

    suspend fun upsertAll() {
        val records = content
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

    companion object {
        private val logger by getLogger()

        init {
            EventHub.subscribe(eventPattern = EventPattern()) { eventType: EventType, entityId: String ->
                val syncId = when (eventType.resource) {
                    Resource.page -> "researchArticles"
                    Resource.report -> "researchReports"
                }
                val entityContentType = eventType.resource.name
                val contentfulToAlgolia = ContentfulToAlgolia(syncId)
                when (eventType.action) {
                    Action.publish -> {
                        try {
                            logger.info("Exporting $entityContentType: $entityId from contentful to algolia")
                            contentfulToAlgolia.upsert(entityId)
                        } catch (e: Exception) {
                            logger.error("Exporting $entityContentType: $entityId from contentful to algolia failed", e)
                        }
                    }
                    Action.unpublish -> {
                        logger.warn("Removing $entityContentType: $entityId from algolia")
                        contentfulToAlgolia.delete(entityId)
                    }
                }
            }
        }
    }
}

fun main() {
    runBlocking {
//        ContentfulToAlgolia("researchArticles").upsertAll()
//        ContentfulToAlgolia("researchArticles").upsert("")
//        ContentfulToAlgolia("researchArticles").delete("")

//        ContentfulToAlgolia("researchReports").upsertAll()
//        ContentfulToAlgolia("researchReports").upsert("")
//        ContentfulToAlgolia("researchReports").delete("")
    }
}