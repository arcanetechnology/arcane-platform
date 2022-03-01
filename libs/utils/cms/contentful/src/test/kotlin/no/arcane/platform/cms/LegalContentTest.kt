package no.arcane.platform.cms

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.Enabled
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe

class LegalContentTest : StringSpec({

    val contentfulConfig = LegalContent.contentfulConfig

    "Check spaceId" {
        LegalContent.checkSpaceId(contentfulConfig.spaceId) shouldBe true
    }

    "Fetch platform.termsAndConditions" {
        LegalContent.fetchLegalEntryMetadata("platform.termsAndConditions") shouldBe
                LegalEntryMetadata(
                    id = "platform.termsAndConditions",
                    version = System.getenv("PLATFORM_TNC_VERSION"),
                    spaceId = contentfulConfig.spaceId,
                    environmentId = System.getenv("PLATFORM_TNC_ENV_ID"),
                    entryId = System.getenv("PLATFORM_TNC_ENTRY_ID"),
                )
    }
})