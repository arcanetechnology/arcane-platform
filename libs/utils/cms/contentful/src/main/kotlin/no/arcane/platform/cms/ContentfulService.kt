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

    override fun getHtml(
        entryKey: String,
    ): String? {

        val entryConfig = contentfulConfig.entries[entryKey]

        if (entryConfig == null) {
            logger.error("entryKey not found: $entryKey")
            return null
        }

        val client = CDAClient.builder()
            .setSpace(contentfulConfig.spaceId)
            .setToken(contentfulConfig.token)
            .build()

        val entry = client
            .fetch(CDAEntry::class.java)
            .one(entryConfig.entryId)

        val node = entry.getField<CDARichDocument>(entryConfig.fieldId)

        val processor = HtmlProcessor()
        val context = HtmlContext()
        return processor.process(context, node)
    }
}