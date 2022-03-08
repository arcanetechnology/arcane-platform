package no.arcane.platform.cms.space.research

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import no.arcane.platform.cms.ContentfulConfig
import no.arcane.platform.utils.config.loadConfig
import no.arcane.platform.utils.logging.getLogger

object ResearchPageValidation {

    private val logger by getLogger()

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

        val researchPagesMetadata = ResearchPagesMetadata(
            contentfulConfig.spaceId,
            contentfulConfig.token,
        )

        val flow: Flow<String> = flow {
            researchPagesMetadata
                .fetchAll()
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

    suspend fun validate(pageId: String): Collection<ValidationError> {
        val page = researchPageForSlack.fetch(pageId) ?: return listOf(ValidationError.PAGE_NOT_FOUND)
        val errors = mutableSetOf<ValidationError>()
        if (page.image.url.endsWith(
                "svg",
                ignoreCase = true
            ) && page.socialMediaImage == null
        ) {
            errors += ValidationError.NON_SVG_IMAGE_NOT_FOUND
        }
        if (page.authors.isEmpty()) {
            errors += ValidationError.AUTHORS_NOT_DEFINED
        }
        if (page.tags.isEmpty()) {
            errors += ValidationError.TAGS_NOT_DEFINED
        }
        return errors
    }
}

enum class ValidationError(val description: String) {
    PAGE_NOT_FOUND("Page not found"),
    NON_SVG_IMAGE_NOT_FOUND("Non-svg image not found"),
    AUTHORS_NOT_DEFINED("Author(s) not defined"),
    TAGS_NOT_DEFINED("Tag(s) not defined"),
}

fun main() {
    runBlocking {
        ResearchPageValidation.validateAll()
    }
}