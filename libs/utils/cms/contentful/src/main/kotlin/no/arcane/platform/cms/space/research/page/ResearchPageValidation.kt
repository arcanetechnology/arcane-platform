package no.arcane.platform.cms.space.research.page

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import no.arcane.platform.cms.ContentfulConfig
import no.arcane.platform.cms.events.Action
import no.arcane.platform.cms.events.EventHub
import no.arcane.platform.cms.events.EventPattern
import no.arcane.platform.cms.events.Resource
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.NotifySlack
import no.arcane.platform.utils.logging.getLogger
import no.arcane.platform.utils.logging.getMarker

object ResearchPageValidation {

    private val logger by getLogger()

    init {
        EventHub.subscribe(EventPattern(Resource.page, Action.publish)) { _, pageId ->
            val errors = validate(pageId = pageId)
            if (errors.isNotEmpty()) {
                logger.warn(
                    NotifySlack.NOTIFY_SLACK_ALERTS.getMarker(),
                    "Validation failed for page[$pageId]: ${errors.map { it.description }}"
                )
            }
        }
    }

    private val contentfulConfig by loadConfig<ContentfulConfig>(
        "contentful",
        "contentfulAlgoliaSync.researchArticles.contentful"
    )

    private val researchPageForSlack by lazy {
        ResearchPageForSlack(
            spaceId = contentfulConfig.spaceId,
            token = contentfulConfig.token,
        )
    }

    suspend fun validateAll() {

        val researchPagesMetadata = ResearchPage(
            spaceId = contentfulConfig.spaceId,
            token = contentfulConfig.token,
        )

        val flow: Flow<String> = flow {
            researchPagesMetadata
                .fetchIdToModifiedMap()
                .keys
                .chunked(50) // rate limit is 55
                .forEach { batch ->
                    batch.forEach { emit(it) }
                    delay(1_000)
                }
        }

        val map = mutableMapOf<String, Collection<ValidationError>>()
        coroutineScope {
            flow.collect { pageId ->
                val errors = validate(pageId)
                if (errors.isNotEmpty()) {
                    map += pageId to errors
                }
            }
        }
        if (map.isNotEmpty()) {
            logger.error("PageIDs with invalid schema: (${map.size})")
            map.forEach { (pageId, errors) ->
                logger.error("PageId[$pageId] - ${errors.map { it.description }}")
            }
        }
    }

    private suspend fun validate(pageId: String): Collection<ValidationError> {
        val page = researchPageForSlack.fetch(pageId) ?: return listOf(ValidationError.PAGE_NOT_FOUND)
        val errors = mutableSetOf<ValidationError>()
        if (page.authors.isEmpty()) {
            errors += ValidationError.AUTHOR_NOT_DEFINED
        } else if (page.authors.size > 4) {
            errors += ValidationError.EXCESS_AUTHORS_DEFINED
        }
        if (page.tags.isEmpty()) {
            errors += ValidationError.TAG_NOT_DEFINED
        } else if (page.tags.size > 10) {
            errors += ValidationError.EXCESS_TAGS_DEFINED
        }
        return errors
    }
}

enum class ValidationError(val description: String) {
    PAGE_NOT_FOUND("Page not found"),
    AUTHOR_NOT_DEFINED("Author not defined"),
    EXCESS_AUTHORS_DEFINED("Excess (>4) Authors defined"),
    TAG_NOT_DEFINED("Tag not defined"),
    EXCESS_TAGS_DEFINED("Excess (>10) Tags defined"),
}

fun main() {
    runBlocking {
        ResearchPageValidation.validateAll()
    }
}