package no.arcane.platform.app.trade.admin.api

import io.ktor.server.application.*
import io.ktor.server.routing.*
import no.arcane.platform.app.trade.admin.api.graphql.graphqlEndpoint
import no.arcane.platform.app.trade.admin.api.rest.restEndpoint

fun Application.module() {
    routing {
        graphqlEndpoint()
        restEndpoint()
    }
}