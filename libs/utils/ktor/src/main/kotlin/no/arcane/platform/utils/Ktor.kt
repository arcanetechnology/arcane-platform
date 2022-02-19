package no.arcane.platform.utils

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Instant
import java.time.temporal.ChronoUnit

fun Application.module() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
            call.application.log.error("Internal Server Error", cause)
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
        route("/ping") {
            get {
                log(application.log)
            }
            post {
                log(application.log)
            }
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