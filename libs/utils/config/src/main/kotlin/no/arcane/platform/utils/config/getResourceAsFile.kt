package no.arcane.platform.utils.config

import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader

fun <R: Any> R.readResource(
    name: String
): String {
    val inputSystem = this::class.java.getResourceAsStream(name) ?: throw FileNotFoundException("resource: $name")
    return BufferedReader(InputStreamReader(inputSystem)).readText()
}