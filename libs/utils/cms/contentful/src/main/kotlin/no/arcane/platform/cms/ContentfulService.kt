package no.arcane.platform.cms

import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.rich.CDARichDocument
import com.contentful.rich.html.HtmlContext
import com.contentful.rich.html.HtmlProcessor
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger

object ContentfulService : CmsService {

    private val logger by getLogger()

    private val contentfulConfig by loadConfig<ContentfulConfig>("contentful", "contentful")

    override suspend fun getHtml(
        id: String,
    ): String? {

        val fetchedMetadata = ContentfulGraphqlClient.fetchLegalEntryMetadata(id)

        if (fetchedMetadata == null) {
            logger.error("cms entry not found: $id")
            return null
        }

        val client = CDAClient.builder()
            .setSpace(contentfulConfig.spaceId)
            .setToken(contentfulConfig.token)
            .build()

        val entry = client
            .fetch(CDAEntry::class.java)
            .one(fetchedMetadata.entryId)

        val node = entry.getField<CDARichDocument>(fetchedMetadata.fieldId)

        val processor = HtmlProcessor()
        val context = HtmlContext()
        return processor.process(context, node)
    }

    override suspend fun check(
        legalEntryMetadata: LegalEntryMetadata
    ): Boolean {

        if (legalEntryMetadata.spaceId != contentfulConfig.spaceId) {
            logger.error("spaceId not found: ${legalEntryMetadata.spaceId}")
            return false
        }

        val fetchedMetadata = ContentfulGraphqlClient.fetchLegalEntryMetadata(legalEntryMetadata.id)
        if (fetchedMetadata != legalEntryMetadata) {
            logger.error("Legal entry metadata do not match. Submitted: $legalEntryMetadata Found in cms: $fetchedMetadata")
            return false
        }
        return true
    }
}