package no.arcane.platform.utils

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import org.slf4j.Logger

suspend fun PipelineContext<Unit, ApplicationCall>.log(logger: Logger) {
    logger.info(
        call.request.headers.entries()
            .filterNot { (name, _) -> name.equals("Authorization", ignoreCase = true) }
            .joinToString { (name, values) ->
                "$name: $values"
            }
    )
    call.respondText("pong")
}