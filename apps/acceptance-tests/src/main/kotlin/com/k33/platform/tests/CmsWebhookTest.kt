package com.k33.platform.tests

import io.kotest.core.spec.style.StringSpec
import io.ktor.client.request.*

class CmsWebhookTest : StringSpec({

    "POST /contentfulEvents - page".config(enabled = false) {
        apiClient.post {
            url (path = "/contentfulEvents")
            headers {
                append("X-Contentful-Topic", "ContentManagement.Entry.publish")
                append("X-CTFL-Entity-ID", System.getenv("TEST_PAGE_ID"))
                append("X-CTFL-Content-Type", "page")
            }
        }
    }

    "POST /contentfulEvents - report" {
        apiClient.post {
            url (path = "/contentfulEvents")
            headers {
                append("X-Contentful-Topic", "ContentManagement.Entry.publish")
                append("X-CTFL-Entity-ID", System.getenv("TEST_REPORT_ID"))
                append("X-CTFL-Content-Type", "report")
            }
        }
    }
})