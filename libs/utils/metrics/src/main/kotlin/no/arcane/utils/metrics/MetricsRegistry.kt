package no.arcane.utils.metrics

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.micrometer.core.instrument.Metrics
import io.micrometer.stackdriver.StackdriverConfig
import io.micrometer.stackdriver.StackdriverMeterRegistry
import java.io.File

internal class StackDriverConfigReader(configFile: File) {
    private val config = ConfigFactory.parseFile(configFile).getConfig("monitoring.metrics")
    internal val stackDriverConfig = object : StackdriverConfig {
        override fun get(key: String): String? = config.extract(key)
        override fun resourceLabels(): MutableMap<String, String> {
            return config.extract("stackdriver.resourceLabels") ?: super.resourceLabels()
        }
    }
}

private object MetricsRegistry {
    init {
        val reader  = StackDriverConfigReader(File("/config/metrics.conf"))
        val registry = StackdriverMeterRegistry.builder(reader.stackDriverConfig).build()
        Metrics.addRegistry(registry)
    }
}
