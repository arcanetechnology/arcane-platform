package no.arcane.platform.cms.content

import no.arcane.platform.cms.ContentfulConfig
import no.arcane.platform.cms.space.research.page.ResearchPage
import no.arcane.platform.cms.space.research.report.ResearchReport
import no.arcane.platform.utils.config.loadConfig

object ContentFactory {
    fun getContent(syncId: String): Content {
        val contentfulConfig by loadConfig<ContentfulConfig>(
            "contentful",
            "contentfulAlgoliaSync.$syncId.contentful"
        )
        return when (syncId) {
            "researchArticles" -> with(contentfulConfig) {
                ResearchPage(
                    spaceId = spaceId,
                    token = token,
                )
            }
            "researchReports" -> with(contentfulConfig) {
                ResearchReport(
                    spaceId = spaceId,
                    token = token,
                )
            }
            else -> throw Exception("Sync ID: $syncId not found in config")
        }
    }
}