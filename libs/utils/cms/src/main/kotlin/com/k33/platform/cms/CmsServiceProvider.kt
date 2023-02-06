package com.k33.platform.cms

fun getCmsService(): Lazy<CmsService> = lazy { ContentfulService }