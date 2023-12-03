package me.timeto.shared

import kotlinx.serialization.json.*

fun zlog(message: Any?) = println(";; ${message.toString().replace("\n", "\n;; ")}")

///
/// Json

fun JsonObject.getInt(key: String): Int = this[key]!!.jsonPrimitive.int
fun JsonObject.getString(key: String): String = this[key]!!.jsonPrimitive.content
fun JsonObject.getStringOrNull(key: String): String? = this[key]!!.jsonPrimitive.contentOrNull
fun JsonObject.getBoolean(key: String): Boolean = this[key]!!.jsonPrimitive.boolean
fun JsonObject.getIntArray(key: String): List<Int> = this[key]!!.jsonArray.map { it.jsonPrimitive.int }

fun JsonArray.getInt(index: Int): Int = this[index].jsonPrimitive.int
fun JsonArray.getIntOrNull(index: Int): Int? = this[index].jsonPrimitive.intOrNull
fun JsonArray.getString(index: Int): String = this[index].jsonPrimitive.content
fun JsonArray.getStringOrNull(index: Int): String? = this[index].jsonPrimitive.contentOrNull

fun List<*>.toJsonArray() = JsonArray(
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
