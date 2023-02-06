package com.k33.platform.cms

interface CmsService {

    suspend fun getHtml(
        id: String
    ): String?

    suspend fun check(
        legalEntryMetadata: LegalEntryMetadata
    ): Boolean
}

data class LegalEntryMetadata(
    val id: String,
    val version: String,
    val spaceId: String,
    val environmentId: String,
    val entryId: String,
    val fieldId: String = "contentOfLegalText",
)