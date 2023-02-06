package com.k33.platform.cms.utils

import com.jayway.jsonpath.PathNotFoundException
import net.andreinc.mapneat.dsl.MapNeat
import net.andreinc.mapneat.model.MapNeatSource
import net.andreinc.mapneat.operation.JsonPathQuery

fun MapNeat.forEachInArrayAt(
    value: String,
    init: MapNeat.() -> Unit
): List<Map<String, Any?>> {
    "__tmp_objects" *= value
    val objects = getObjectMap()["__tmp_objects"] as List<Any>
    -"__tmp_objects"
    return objects.map {
        json(MapNeatSource.fromObject(it), init)
    }
}

fun JsonPathQuery.richToPlainText(expression: String) {
    this.expression = expression
    processor = ::richToPlainTextProcessor
}

fun richToPlainTextProcessor(input: Any): Any {
    return when (input) {
        is LinkedHashMap<*, *> -> getPlainText(input as LinkedHashMap<String, Any>)
        is String -> input
        else -> {
            throw Exception("Unknown type: ${input.javaClass}")
        }
    }.replace(Regex("\\s+"), " ")
}

fun MapNeat.optional(init: MapNeat.() -> Unit) {
    try {
        init()
    } catch (_: PathNotFoundException) {
    }
}

fun getPlainText(map: LinkedHashMap<String, Any>): String {
    return if (map["nodeType"] == "text") {
        map["value"] as? String
    } else {
        val list = map["content"] as? List<LinkedHashMap<String, Any>>
        list?.joinToString(separator = "") {
            getPlainText(it)
        }
    } ?: ""
}