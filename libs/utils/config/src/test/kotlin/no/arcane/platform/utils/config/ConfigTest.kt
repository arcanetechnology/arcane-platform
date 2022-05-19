package no.arcane.platform.utils.config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.FileNotFoundException

class ConfigTest : StringSpec({

    "config not found" {
        val exception = shouldThrow<FileNotFoundException> {
            val config by loadConfig<String>("missing", "null")
            println(config)
        }

        exception.message shouldBe "Config file not found - resource:/missing.conf @ null"
    }

    "root config" {
        val testValue by getConfig("test")
        testValue.getStringList("TEST_CONFIG_PARAM") shouldBe listOf("test-value")
    }
})