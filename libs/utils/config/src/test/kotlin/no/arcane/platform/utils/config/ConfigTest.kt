package no.arcane.platform.utils.config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.FileNotFoundException

class ConfigTest : StringSpec({

    "config not found" {
        val exception = shouldThrow<FileNotFoundException> {
            val config by loadConfig<String>("missing", "null", "unit-test")
            println(config)
        }

        exception.message shouldBe "Config files not found for env: unit-test - [file:/config/missing-unit-test.conf, resource:/missing-unit-test.conf @ null, resource:/missing.conf @ null]"
    }
})