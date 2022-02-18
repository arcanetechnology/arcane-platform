package no.arcane.platform.utils

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit

private val logger by lazy { LoggerFactory.getLogger("no.arcane.platform.utils.Ktor") }

fun Application.module() {
    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            logger.error("Internal Server Error", cause)
            throw cause
        }
    }
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/ping") {
            call.respondText("pong")
        }
        get("/utc") {
            call.respondText(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
        }
    }
    environment.monitor.subscribe(ApplicationStarting) {
        logger.info("Application starting...")
    }
    environment.monitor.subscribe(ApplicationStarted) {
        logger.info("Application started.")
    }
    environment.monitor.subscribe(ApplicationStopping) {
        logger.info("Application stopping...")
    }
    environment.monitor.subscribe(ApplicationStopped) {
        logger.info("Application stopped.")
    }
}
