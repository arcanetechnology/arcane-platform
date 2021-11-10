package no.arcane.platform.cms

data class ContentfulConfig(
    val spaceId: String,
    val token: String,
    val entries: Map<String, Entry>
)

data class Entry(
    val entryId: String,
    val fieldId: String,
)