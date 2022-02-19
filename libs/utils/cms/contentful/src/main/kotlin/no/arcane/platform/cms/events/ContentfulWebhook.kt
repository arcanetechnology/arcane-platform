package no.arcane.platform.cms.events

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.arcane.platform.cms.space.research.page.ResearchPageValidation
import no.arcane.platform.cms.sync.ContentfulToAlgolia
import no.arcane.platform.utils.logging.getLogger
import no.arcane.platform.utils.logging.logWithMDC

fun Application.module() {

    val logger by getLogger()

    // trigger init to subscribe to EventHub
    SlackNotification
    ResearchPageValidation
    ContentfulToAlgolia

    routing {
        post("contentfulEvents") {
            val topic = call.request.header("X-Contentful-Topic")!!
            val entityContentType = call.request.header("X-CTFL-Content-Type") ?: return@post
            val entityId = call.request.header("X-CTFL-Entity-ID") ?: return@post
            val (type, action) = Regex("^ContentManagement.([a-zA-Z]+).([a-zA-Z]+)$").find(topic)!!.destructured
            logWithMDC("${entityContentType}Id" to entityId) {
                logger.info("Received contentful event: $type.$action for $entityContentType/$entityId")
                if (type == "Entry") {
                    val eventType = EventType(
                        resource = Resource.valueOf(entityContentType),
                        action = Action.valueOf(action)
                    )
                    EventHub.notify(
                        eventType = eventType,
                        id = entityId,
                    )
                } else if (type == "ContentType") {
                    logger.warn("Content type is modified")
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

