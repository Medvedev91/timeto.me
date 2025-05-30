package me.timeto.shared

import me.timeto.appdbsq.TimetomeDB
import me.timeto.shared.db.DB_NAME
import me.timeto.shared.misc.SystemInfo
import platform.Foundation.NSBundle
import platform.WatchKit.WKInterfaceDevice

fun initKmpWatchOS() {
    val systemInfo = SystemInfo(
        build = (NSBundle.mainBundle.infoDictionary!!["CFBundleVersion"] as String).toInt(),
        version = NSBundle.mainBundle.infoDictionary!!["CFBundleShortVersionString"] as String,
        os = SystemInfo.Os.Watchos(WKInterfaceDevice.currentDevice().systemVersion),
        device = machineIdentifier(),
        flavor = null,
    )
    initKmp(createNativeDriver(DB_NAME, TimetomeDB.Schema), systemInfo)
}
