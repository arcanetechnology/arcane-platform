package no.arcane.platform.utils.analytics

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.arcane.platform.utils.logging.getLogger
import org.slf4j.LoggerFactory

object GoogleAnalyticsService {

    private val logger by getLogger()

    suspend fun check(
        clientId: String,
        userAnalyticsId: String,
        events: Collection<Event>,
    ) {
        val request = Request(
            clientId = clientId,
            userAnalyticsId = userAnalyticsId,
            events = events
        )
        if (!request.verify()) {
            return
        }
        val validationMessages: List<ValidationMessage> = googleAnalyticsClient.post {
            url(path = "/debug/mp/collect")
            body = request
        }
        if (validationMessages.isNotEmpty()) {
            logger.error(validationMessages.joinToString())
            return
        }
    }

    suspend fun submit(
        clientId: String,
        userAnalyticsId: String,
        events: Collection<Event>,
    ) {
        val request = Request(
            clientId = clientId,
            userAnalyticsId = userAnalyticsId,
            events = events
        )
        googleAnalyticsClient.post<Unit> {
            body = request
        }
    }
}

private val googleAnalyticsClient = HttpClient(CIO) {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
    install(UserAgent) {
        agent = "arcane-platform-app"
    }
    install(JsonFeature)
    defaultRequest {
        url("https://www.google-analytics.com/mp/collect")
        contentType(ContentType.Application.Json)
        parameter("api_secret", System.getenv("GOOGLE_ANALYTICS_API_KEY"))
        parameter("measurement_id", System.getenv("GOOGLE_ANALYTICS_MEASUREMENT_ID"))
    }
}

/**
 * Ref: https://developers.google.com/analytics/devguides/collection/protocol/ga4/reference
 */
@Serializable
private data class Request(
    @SerialName("client_id") val clientId: String,
    @SerialName("user_id") val userAnalyticsId: String,
    @SerialName("timestamp_micros") val timestampInMicroseconds: Long? = null,
    @SerialName("user_properties") val userProperties: Map<String, String>? = null,
    @SerialName("non_personalized_ads") val nonPersonalizedAds: Boolean? = null,
    val events: Collection<Event>,
)

@Serializable
data class Event(
    val name: String,
    val params: Map<String, String>? = null
)

@Serializable
private data class ValidationMessage(
    val fieldPath: String? = null,
    val description: String? = null,
    val validationCode: ValidationCode? = null,
)

enum class ValidationCode {
    VALUE_INVALID,
    VALUE_REQUIRED,
    NAME_INVALID,
    NAME_RESERVED,
    VALUE_OUT_OF_BOUNDS,
    EXCEEDED_MAX_ENTITIES,
    NAME_DUPLICATED,
}

private val logger by lazy {
    LoggerFactory.getLogger(Request::class.java)
}

private fun Request.verify(): Boolean {
    val validationErrors = mutableSetOf<String>()

    if (events.size > 25) {
        validationErrors += "Requests can have a maximum of 25 events."
    }

    val regex = Regex("^[a-zA-Z][a-zA-Z0-9_]{0,39}$")
    fun verifyEventNames() {
        val eventNameSet = events.map(Event::name).toSet()
        val foundInvalidEventNames = eventNameSet.filterNot { it.matches(regex) }
        if (foundInvalidEventNames.isNotEmpty()) {
            validationErrors += "Found invalid events names: $foundInvalidEventNames"
        }
        val foundWithReservedNames = eventNameSet - reservedEventNames
        if (foundWithReservedNames.isNotEmpty()) {
            validationErrors += "Found events with reserved names: $foundWithReservedNames"
        }
    }

    fun verifyEventParamNames() {
        val paramNamesSet = events
            .mapNotNull(Event::params)
            .flatMap(Map<String, String>::keys)
            .toSet()
        val foundInvalidParamNames = paramNamesSet.filterNot { it.matches(regex) }
        if (foundInvalidParamNames.isNotEmpty()) {
            validationErrors += "Found invalid param names: $foundInvalidParamNames"
        }
        if (paramNamesSet.contains(reservedEventParamName)) {
            validationErrors += "Found event params wth reserved name: $reservedEventParamName"
        }
        val foundWithReservedPrefix = paramNamesSet.filter {
            reservedEventParamNamesPrefix.any { prefix -> it.startsWith(prefix) }
        }
        if (foundWithReservedPrefix.isNotEmpty()) {
            validationErrors += "Found event params with reserved prefix: $foundWithReservedPrefix"
        }
    }

    fun verifyEventParamValues() {
        val foundLongEventParamValues = events
            .mapNotNull(Event::params)
            .flatMap(Map<String, String>::values)
            .toSet()
            .filter { it.length > 100 }
        if (foundLongEventParamValues.isNotEmpty()) {
            validationErrors += "Parameter values must be 100 character or fewer. Found $foundLongEventParamValues"
        }
    }

    fun verifyEventParams() {
        if (events.any { (it.params?.size ?: 0) > 25 }) {
            validationErrors += "Events can have a maximum of 25 parameters."
        }
        verifyEventParamNames()
        verifyEventParamValues()
    }

    fun verifyEvents() {
        verifyEventNames()
        verifyEventParams()
    }

    fun verifyUserProperties() {
        if (!userProperties.isNullOrEmpty()) {
            if (userProperties.size > 25) {
                validationErrors += "Events can have a maximum of 25 user properties."
            }
            val foundLongUserPropertyNames = userProperties.keys.filter { it.length > 24 }
            if (foundLongUserPropertyNames.isNotEmpty()) {
                validationErrors += "User property names must be 24 characters or fewer. Found $foundLongUserPropertyNames"
            }
            val foundLongUserPropertyValues = userProperties.values.filter { it.length > 36 }
            if (foundLongUserPropertyValues.isNotEmpty()) {
                validationErrors += "User property values must be 36 characters or fewer. Found $foundLongUserPropertyValues"
            }
            val foundWithReservedNames = userProperties.keys.map(String::lowercase).toSet() - reservedUserPropertyNames
            if (foundWithReservedNames.isNotEmpty()) {
                validationErrors += "Found userProperties with reserved names: $foundWithReservedNames"
            }
            val foundWithReservedPrefix = userProperties.keys.map(String::lowercase).toSet().filter {
                reservedUserPropertyNamesPrefix.any { prefix -> it.startsWith(prefix) }
            }
            if (foundWithReservedPrefix.isNotEmpty()) {
                validationErrors += "Found userProperties with reserved prefix: $foundWithReservedNames"
            }
        }
    }

    verifyEvents()
    verifyUserProperties()

    if (validationErrors.isNotEmpty()) {
        logger.error(validationErrors.joinToString())
        return false
    }
    return true
}

private val reservedEventNames = setOf(
    "ad_activeview",
    "ad_click",
    "ad_exposure",
    "ad_impression",
    "ad_query",
    "adunit_exposure",
    "app_clear_data",
    "app_install",
    "app_update",
    "app_remove",
    "error",
    "first_open",
    "first_visit",
    "in_app_purchase",
    "notification_dismiss",
    "notification_foreground",
    "notification_open",
    "notification_receive",
    "os_update",
    "screen_view",
    "session_start",
    "user_engagement",
)

private const val reservedEventParamName = "firebase_conversion"

private val reservedEventParamNamesPrefix = setOf(
    "google_",
    "ga_",
    "firebase_",
)

private val reservedUserPropertyNames = setOf(
    "first_open_time",
    "first_visit_time",
    "last_deep_link_referrer",
    "user_id",
    "first_open_after_install",
)

private val reservedUserPropertyNamesPrefix = setOf(
    "google_",
    "ga_",
    "firebase_",
)