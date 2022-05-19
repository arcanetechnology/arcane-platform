package no.arcane.platform.cms.utils

import io.kotest.core.spec.style.StringSpec
import net.andreinc.mapneat.dsl.json

class MapNeatOptionalTest : StringSpec({

    "ignore optional" {
        val jsonValue = """
        {
            "old": "present"
        }
    """.trimIndent()
        val transformed = json(jsonValue) {
            "new" *= "old"
            optional {
                "new_optional" *= "old_optional"
            }
        }
        println(transformed)
    }
})