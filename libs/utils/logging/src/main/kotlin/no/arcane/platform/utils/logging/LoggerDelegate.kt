package no.arcane.platform.utils.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This is a lazy delegate slf4j logger factory method using kotlin's property delegates and extension functions.
 *
 * Inside a class, get a logger by declaring: `private val logger by getLogger()`
 *
 */
fun <R: Any> R.getLogger(): Lazy<Logger> = lazy {
    LoggerFactory.getLogger(this::class.java)
}