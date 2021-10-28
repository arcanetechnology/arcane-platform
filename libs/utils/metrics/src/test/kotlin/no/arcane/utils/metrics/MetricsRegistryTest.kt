package no.arcane.utils.metrics

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micrometer.stackdriver.StackdriverConfig
import io.micrometer.stackdriver.StackdriverMeterRegistry
import java.io.File
import java.time.Duration

class MetricsRegistryTest : StringSpec({

    val configFile = File("src/test/resources/metrics.conf")
    val stackDriverConfigReader = StackDriverConfigReader(configFile)
    val stackDriverConfig = stackDriverConfigReader.stackDriverConfig

    "config file should exist" {
        configFile.exists()
    }

    "read config - project id" {
        stackDriverConfig.projectId() shouldBe "arcane-platform-dev"
    }
    "read config - resource type" {
        stackDriverConfig.resourceType() shouldBe "generic_task"
    }
    "read config - enabled" {
        stackDriverConfig.enabled() shouldBe false
    }

    "read config - step" {
        stackDriverConfig.step() shouldBe Duration.ofMinutes(1)
    }

    "read config - resourceLabels" {
        stackDriverConfig.resourceLabels() shouldBe mapOf("application" to "arcane-platform-app")
    }
})