package no.arcane.platform.utils.metrics

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Duration

class MetricsRegistryTest : StringSpec({

    val stackDriverConfigReader = StackDriverConfigReader()
    val stackDriverConfig = stackDriverConfigReader.stackDriverConfig

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