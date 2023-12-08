package me.timeto.shared

import java.util.*

//
// Time

actual fun time(): Int = (System.currentTimeMillis() / 1_000).toInt()

actual fun timeMls(): Long = System.currentTimeMillis()

actual fun getLocalUtcOffset(): Int = TimeZone.getDefault().getOffset(timeMls()) / 1_000
