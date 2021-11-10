package no.arcane.platform.cms

fun getCmsService(): Lazy<CmsService> = lazy { ContentfulService }