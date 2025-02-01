package me.timeto.shared

import me.timeto.appdbsq.TimetomeDB
import me.timeto.shared.db.DB_NAME
import me.timeto.shared.misc.SystemInfo
import platform.Foundation.NSBundle
import platform.WatchKit.WKInterfaceDevice

internal actual val REPORT_API_TITLE = "⌚ Watch OS"

fun initKmpWatchOS() {
    val deviceData = DeviceData(
        build = (NSBundle.mainBundle.infoDictionary!!["CFBundleVersion"] as String).toInt(),
        os = "watchos-${WKInterfaceDevice.currentDevice().systemVersion}",
        device = machineIdentifier(),
        flavor = null,
    )
    initKmp(createNativeDriver(DB_NAME, TimetomeDB.Schema), deviceData)
}

actual fun getResourceContent(file: String, type: String): String {
    TODO("WatchOS getResourceContent() not implemented")
}
