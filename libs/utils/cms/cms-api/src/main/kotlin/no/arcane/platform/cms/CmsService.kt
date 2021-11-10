package no.arcane.platform.cms

interface CmsService {
    fun getHtml(
        entryKey: String
    ): String?
}
