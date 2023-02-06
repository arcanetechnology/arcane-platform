package com.k33.platform.cms

import com.k33.platform.utils.logging.getLogger

object ContentfulService : CmsService {

    private val logger by getLogger()

    override suspend fun getHtml(
        id: String,
    ): String? = LegalContent.getRichTextAsHtml(id)

    override suspend fun check(
        legalEntryMetadata: LegalEntryMetadata
    ): Boolean {

        if (!LegalContent.checkSpaceId(legalEntryMetadata.spaceId)) {
            return false
        }

        val fetchedMetadata = LegalContent.fetchLegalEntryMetadata(legalEntryMetadata.id)
        if (fetchedMetadata != legalEntryMetadata) {
            logger.error("Legal entry metadata do not match. Submitted: $legalEntryMetadata Found in cms: $fetchedMetadata")
            return false
        }
        return true
    }
}