package no.arcane.platform.utils

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import java.time.Instant
import java.time.temporal.ChronoUnit

fun Application.module() {
    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            log.error("Internal Server Error", cause)
            throw cause
        }
    }
    install(ContentNegotiation) {
        json()
    }
    install(CallId) {
        val prefix = System.getenv("GCP_PROJECT_ID")
            ?.let { gcpProjectId -> "projects/$gcpProjectId/traces/" }
            ?: ""
        retrieve { call: ApplicationCall ->
            call.request.header("traceparent")
                ?.split("-")
                ?.getOrNull(1)
                ?.let { traceId -> prefix + traceId }
        }
    }
    routing {
        get("/ping") {
            log.info(
                call.request.headers.entries()
                    .filterNot { (name, _) -> name.equals("Authorization", ignoreCase = true) }
                    .joinToString { (name, values) ->
                        "$name: $values"
                    }
            )
            call.respondText("pong")
        }
        get("/utc") {
            call.respondText(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
        }
    }
    environment.monitor.subscribe(ApplicationStarting) {
        log.info("Application starting...")
    }
    environment.monitor.subscribe(ApplicationStarted) {
        log.info("Application started.")
    }
    environment.monitor.subscribe(ApplicationStopping) {
        log.info("Application stopping...")
    }
    environment.monitor.subscribe(ApplicationStopped) {
        log.info("Application stopped.")
    }
}