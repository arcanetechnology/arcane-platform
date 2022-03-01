package no.arcane.platform.cms.sync

import com.algolia.search.client.ClientSearch
import com.algolia.search.dsl.attributesToRetrieve
import com.algolia.search.dsl.query
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.ObjectID
import com.algolia.search.model.indexing.BatchOperation
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class AlgoliaClient(
    applicationId: ApplicationID,
    apiKey: APIKey,
    indexName: IndexName,
) {

    private val index by lazy {
        val client = ClientSearch(
            applicationId,
            apiKey,
        )
        client.initIndex(indexName)
    }

    suspend fun upsert(
        objectID: ObjectID,
        record: JsonObject,
    ) {
        index.replaceObject(
            objectID,
            record,
        )
    }

    suspend fun batchUpsert(
        records: List<Pair<ObjectID, JsonObject>>
    ) {
        index.replaceObjects(records)
    }

    suspend fun delete(
        objectID: ObjectID,
    ) {
        index.deleteObject(objectID)
    }

    suspend fun getAllIds(): Map<String, String> {
        val query = query("") {
            attributesToRetrieve {
                +"objectID"
                +"publishedAt"
            }
        }
        return index.browseObjects(query)
            .flatMap {
                it.hits.map { hit ->
                    hit.json["objectID"]!!.jsonPrimitive.content to hit.json["publishedAt"]!!.jsonPrimitive.content
                }
            }
            .toMap()
    }
}