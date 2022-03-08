package no.arcane.platform.cms.sync

import no.arcane.platform.cms.ContentfulConfig

data class ContentfulAlgoliaSyncConfig(
    val contentful: ContentfulConfig,
    val algolia: AlgoliaConfig,
)

data class AlgoliaConfig(
    val applicationId: String,
    val apiKey: String,
)