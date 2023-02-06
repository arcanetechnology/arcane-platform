package com.k33.platform.cms.content

import com.k33.platform.cms.ContentfulConfig
import com.k33.platform.cms.space.research.page.ResearchPage
import com.k33.platform.cms.space.research.report.ResearchReport
import com.k33.platform.utils.config.loadConfig

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