package no.arcane.platform.services

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.time.Instant
import java.time.temporal.ChronoUnit

private val logger by lazy { LoggerFactory.getLogger("no.arcane.platform.services.Ktor") }

fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
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
