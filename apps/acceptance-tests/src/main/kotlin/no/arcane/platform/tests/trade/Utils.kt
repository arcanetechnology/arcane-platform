package no.arcane.platform.tests.trade

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.arcane.platform.tests.trade.AdminHttpClient.logger
import no.arcane.platform.tests.utils.apiClient
import no.arcane.platform.tests.utils.appendAdminIdToken
import no.arcane.platform.utils.logging.getLogger

object AdminHttpClient {
    val logger by getLogger()
}

suspend fun get(path: String): HttpResponse {
    val response = apiClient.get {
        url(path = path)
        headers {
            appendAdminIdToken()
        }
    }
    if (response.status != HttpStatusCode.OK) {
        logger.error(response.bodyAsText())
    }
    return response
}

suspend fun post(path: String): HttpResponse {
    val response = apiClient.post {
        url(path = path)
        headers {
            appendAdminIdToken()
        }
    }
    if (response.status != HttpStatusCode.OK) {
        logger.error(response.bodyAsText())
    }
    return response
}

suspend fun post(path: String, body: Any): HttpResponse {
    val response = apiClient.post {
        url(path = path)
        headers {
            appendAdminIdToken()
        }
        contentType(ContentType.Application.Json)
        setBody(body)
    }
    if (response.status != HttpStatusCode.OK) {
        logger.error(response.bodyAsText())
    }
    return response
}