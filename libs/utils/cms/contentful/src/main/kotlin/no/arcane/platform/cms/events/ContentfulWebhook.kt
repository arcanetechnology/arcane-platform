package no.arcane.platform.cms.events

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.arcane.platform.cms.sync.ContentfulToAlgolia
import no.arcane.platform.utils.logging.getLogger

fun Application.module() {

    val logger by getLogger()

    val contentfulToAlgolia = ContentfulToAlgolia("researchArticles")

    routing {
        post("contentfulEvents") {
            val topic = call.request.header("X-Contentful-Topic")!!
            val entityContentType = call.request.header("X-CTFL-Content-Type") ?: return@post
            val entityId = call.request.header("X-CTFL-Entity-ID") ?: return@post
            val (type, action) = Regex("^ContentManagement.([a-zA-Z]+).([a-zA-Z]+)$").find(topic)!!.destructured
            logger.info("Received contentful event: $type.$action for $entityContentType/$entityId")
            if (type == "Entry" && entityContentType == "page") {
                when (action) {
                    "publish" -> {
                        logger.info("Exporting page: $entityId from contentful to algolia")
                        contentfulToAlgolia.upsert(entityId)
                    }
                    "unpublish", "delete" -> {
                        logger.warn("Removing page: $entityId from algolia")
                        contentfulToAlgolia.delete(entityId)
                    }
                }
            } else if (type == "ContentType") {
                logger.warn("Content type is modified")
            }
            call.respond("")
        }
    }
}