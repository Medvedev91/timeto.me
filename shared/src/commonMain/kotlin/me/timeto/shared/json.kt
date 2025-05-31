package me.timeto.shared

import kotlinx.serialization.json.*

internal fun JsonObject.getInt(key: String): Int = this[key]!!.jsonPrimitive.int
internal fun JsonObject.getDouble(key: String): Double = this[key]!!.jsonPrimitive.double
internal fun JsonObject.getDoubleOrNull(key: String): Double? = this[key]!!.jsonPrimitive.doubleOrNull
internal fun JsonObject.getString(key: String): String = this[key]!!.jsonPrimitive.content
internal fun JsonObject.getStringOrNull(key: String): String? = this[key]!!.jsonPrimitive.contentOrNull
internal fun JsonObject.getBoolean(key: String): Boolean = this[key]!!.jsonPrimitive.boolean
internal fun JsonObject.getBooleanOrNull(key: String): Boolean? = this[key]!!.jsonPrimitive.booleanOrNull
internal fun JsonObject.getIntArray(key: String): List<Int> = this[key]!!.jsonArray.map { it.jsonPrimitive.int }

internal fun JsonArray.getInt(index: Int): Int = this[index].jsonPrimitive.int
internal fun JsonArray.getIntOrNull(index: Int): Int? = this[index].jsonPrimitive.intOrNull
internal fun JsonArray.getString(index: Int): String = this[index].jsonPrimitive.content
internal fun JsonArray.getStringOrNull(index: Int): String? = this[index].jsonPrimitive.contentOrNull

internal fun List<*>.toJsonArray() = JsonArray(
    this.map { item ->
        when (item) {
            is JsonElement -> item
            is String -> JsonPrimitive(item)
            is Int -> JsonPrimitive(item)
            null -> JsonNull
            else -> throw Exception() // todo report
        }
    }
)
