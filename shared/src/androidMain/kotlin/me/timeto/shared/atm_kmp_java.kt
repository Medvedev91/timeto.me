package me.timeto.shared

//
// Time

actual fun time(): Int = (System.currentTimeMillis() / 1_000).toInt()

actual fun timeMls(): Long = System.currentTimeMillis()
