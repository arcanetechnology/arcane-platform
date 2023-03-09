package com.k33.platform.utils.metrics

import com.k33.platform.utils.config.getConfig
import io.github.config4k.extract
import io.micrometer.core.instrument.Metrics
import io.micrometer.stackdriver.StackdriverConfig
import io.micrometer.stackdriver.StackdriverMeterRegistry

internal class StackDriverConfigReader {
    private val config by getConfig(name = "metrics", path = "monitoring.metrics")
    internal val stackDriverConfig = object : StackdriverConfig {
        override fun get(key: String): String? = config.extract(key)
        override fun resourceLabels(): MutableMap<String, String> {
            return config.extract("stackdriver.resourceLabels") ?: super.resourceLabels()
        }
    }
}

private object MetricsRegistry {
    init {
        val reader  = StackDriverConfigReader()
        val registry = StackdriverMeterRegistry.builder(reader.stackDriverConfig).build()
        Metrics.addRegistry(registry)
    }
}
