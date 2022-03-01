package no.arcane.platform.cms.clients

import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.rich.CDARichDocument
import com.contentful.rich.html.HtmlContext
import com.contentful.rich.html.HtmlProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentfulClient(
    spaceId: String,
    token: String,
) {
    private val client by lazy {
        CDAClient.builder()
            .setSpace(spaceId)
            .setToken(token)
            .build()
    }

    suspend fun getRichTextAsHtml(
        entryId: String,
        fieldId: String,
    ): String? {
        val entry = withContext(Dispatchers.IO) {
            client
                .fetch(CDAEntry::class.java)
                .one(entryId)
        }

        val node = entry.getField<CDARichDocument>(fieldId)

        val processor = HtmlProcessor()
        val context = HtmlContext()
        return processor.process(context, node)
    }
}