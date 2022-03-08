package no.arcane.platform.cms.events

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import no.arcane.platform.cms.space.research.ResearchPageValidation
import no.arcane.platform.cms.sync.ContentfulToAlgolia
import no.arcane.platform.utils.logging.NotifySlack
import no.arcane.platform.utils.logging.getLogger
import no.arcane.platform.utils.logging.getMarker
import no.arcane.platform.utils.logging.logWithMDC

fun Application.module() {

    val logger by getLogger()

    val contentfulToAlgolia by lazy { ContentfulToAlgolia("researchArticles") }

    routing {
        post("contentfulEvents") {
            val topic = call.request.header("X-Contentful-Topic")!!
            val entityContentType = call.request.header("X-CTFL-Content-Type") ?: return@post
            val entityId = call.request.header("X-CTFL-Entity-ID") ?: return@post
            val (type, action) = Regex("^ContentManagement.([a-zA-Z]+).([a-zA-Z]+)$").find(topic)!!.destructured
            logger.info("Received contentful event: $type.$action for $entityContentType/$entityId")
            if (type == "Entry" && entityContentType == "page") {
                logWithMDC("pageId" to entityId) {
                    when (action) {
                        "publish" -> {
                            coroutineScope {
                                launch {
                                    try {
                                        SlackNotification.notifySlack(pageId = entityId)
                                    } catch (e: Exception) {
                                        logger.error("Sending notification failed", e)
                                    }
                                }
                                launch {
                                    try {
                                        val errors = ResearchPageValidation.validate(pageId = entityId)
                                        logger.warn(
                                            NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(),
                                            "Validation failed for page[$entityId]: ${errors.map { it.description }}"
                                        )
                                    } catch (e: Exception) {
                                        logger.error("Sending notification failed", e)
                                    }
                                }
                                try {
                                    logger.info("Exporting page: $entityId from contentful to algolia")
                                    contentfulToAlgolia.upsert(entityId)
                                } catch (e: Exception) {
                                    logger.error("Exporting page $entityId from contentful to algolia failed", e)
                                }
                            }
                        }
                        "unpublish", "delete" -> {
                            logger.warn("Removing page: $entityId from algolia")
                            contentfulToAlgolia.delete(entityId)
                        }
                    }
                }
            } else if (type == "ContentType") {
                logger.warn("Content type is modified")
            }
            call.respond("")
        }
    }
}