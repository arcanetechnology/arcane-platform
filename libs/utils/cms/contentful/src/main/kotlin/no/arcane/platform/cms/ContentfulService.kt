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

    override fun check(
        entryKey: String,
        spaceId: String,
        environmentId: String,
        entryId: String,
        fieldId: String,
        version: String,
    ): Boolean {

        val errors = mutableSetOf<String>()

        if (contentfulConfig.spaceId != spaceId) {
            errors += "spaceId not found: $spaceId"
        }

        if (contentfulConfig.environmentId != environmentId) {
            errors += "environmentId not found: $environmentId"
        }

        val entryConfig = contentfulConfig.entries[entryKey]

        if (entryConfig == null) {
            errors += "entryKey not found: $entryKey"
        }
        else {
            if (entryConfig.entryId != entryId) {
                errors += "Entry Id does not match. Expected: ${entryConfig.entryId} Found: $entryId"
            }

            if (entryConfig.fieldId != fieldId) {
                errors += "Field Id does not match. Expected: ${entryConfig.fieldId} Found: $fieldId"
            }
        }

        if (errors.isNotEmpty()) {
            logger.error(errors.joinToString())
        }

        return errors.isEmpty()
    }
}