package com.k33.platform.utils.logging

import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.MDC

suspend fun <T> logWithMDC(
    vararg pair: Pair<String, String>,
    block: suspend () -> T
): T = withContext(
    MDCContext(
        (MDC.getCopyOfContextMap() ?: emptyMap<String, String>()) + pair.toMap()
    )
) {
    block()
}
