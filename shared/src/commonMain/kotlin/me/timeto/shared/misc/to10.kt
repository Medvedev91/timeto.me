package me.timeto.shared.misc

fun Boolean.toInt10(): Int = if (this) 1 else 0
fun Int.toBoolean10(): Boolean = this != 0
fun Boolean.toString10(): String = if (this) "1" else "0"
fun String.toBoolean10(): Boolean = when (this) {
    "1" -> true
    "0" -> false
    else -> throw Exception()
}
