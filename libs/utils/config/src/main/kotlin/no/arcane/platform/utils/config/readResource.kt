package no.arcane.platform.utils.config

import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader

fun <R: Any> R.getResourceReader(
    name: String
): BufferedReader {
    val inputSystem = this::class.java.getResourceAsStream(name) ?: throw FileNotFoundException("resource: $name")
    return BufferedReader(InputStreamReader(inputSystem))
}

fun <R: Any> R.readResourceLines(
    name: String
): List<String> = getResourceReader(name).readLines()

fun <R: Any> R.readResourceText(
    name: String
): String = getResourceReader(name).readText()

fun <R: Any> R.readResourceWithoutWhitespace(
    name: String
): String = readResourceText(name).replace(Regex("\\s+"), " ")

fun <R: Any> R.lazyResourceWithoutWhitespace(name: String): Lazy<String> = lazy {
    readResourceWithoutWhitespace(name)
}