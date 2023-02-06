package com.k33.platform.utils.logging

import org.slf4j.Marker
import org.slf4j.MarkerFactory

enum class NotifySlack {

    // sorted alphabetically.
    NOTIFY_SLACK_ALERTS,
    NOTIFY_SLACK_GENERAL,
    NOTIFY_SLACK_INVEST,
    NOTIFY_SLACK_PRODUCT,
    NOTIFY_SLACK_RESEARCH,
}

fun NotifySlack.getMarker(): Marker = MarkerFactory.getMarker(this.name)

fun Marker.asNotifySlack(): NotifySlack? = try {
    NotifySlack.valueOf(name)
} catch (e: Exception) {
    null
}