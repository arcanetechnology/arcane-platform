@file:Suppress("EnumEntryName")

package no.arcane.platform.cms.events

enum class Resource {
    page,
    report,
}

enum class Action {
    publish,
    unpublish,
}

data class EventType(
    val resource: Resource,
    val action: Action,
)

data class EventPattern(
    val resource: Resource? = null,
    val action: Action? = null,
)
