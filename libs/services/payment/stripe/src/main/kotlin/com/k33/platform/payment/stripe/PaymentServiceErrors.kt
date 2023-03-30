package com.k33.platform.payment.stripe

import io.ktor.http.*

sealed class PaymentServiceError(
    val httpStatusCode: HttpStatusCode,
    override val message: String,
) : Exception(message)

object AlreadySubscribed : PaymentServiceError(
    httpStatusCode = HttpStatusCode.Conflict,
    message = "Already subscribed",
)

class NotFound(message: String) : PaymentServiceError(
    httpStatusCode = HttpStatusCode.NotFound,
    message = message,
)

class BadRequest(message: String) : PaymentServiceError(
    httpStatusCode = HttpStatusCode.BadRequest,
    message = message,
)

class ServiceUnavailable(message: String) : PaymentServiceError(
    httpStatusCode = HttpStatusCode.ServiceUnavailable,
    message = message,
)

object TooManyRequests: PaymentServiceError(
    httpStatusCode = HttpStatusCode.TooManyRequests,
    message = "Too many requests. Please try again after some time.",
)

object InternalServerError: PaymentServiceError(
    httpStatusCode = HttpStatusCode.InternalServerError,
    message = "Internal Server Error",
)