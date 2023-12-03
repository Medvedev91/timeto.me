package me.timeto.shared

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

//
// Time

actual fun time(): Int = NSDate().timeIntervalSince1970.toInt()

actual fun timeMls(): Long = (NSDate().timeIntervalSince1970 * 1_000).toLong()
