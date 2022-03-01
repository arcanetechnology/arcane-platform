package no.arcane.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.ktor.client.request.*

class CmsWebhook : StringSpec({

    "POST /contentfulEvents".config(enabled = false) {
        apiClient.post {
            url (path = "/contentfulEvents")
            headers {
                append("X-Contentful-Topic", "ContentManagement.Entry.published")
                append("X-CTFL-Entity-ID", "")
                append("X-CTFL-Content-Type", "page")
            }
        }
    }
})