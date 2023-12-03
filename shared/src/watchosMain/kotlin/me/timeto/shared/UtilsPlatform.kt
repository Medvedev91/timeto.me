package me.timeto.shared

import platform.Foundation.NSBundle
import platform.WatchKit.WKInterfaceDevice

internal actual val REPORT_API_TITLE = "⌚ Watch OS"

fun initKmmWatchOS(deviceName: String) {
    val deviceData = DeviceData(
        build = (NSBundle.mainBundle.infoDictionary!!["CFBundleVersion"] as String).toInt(),
        os = "watchos-${WKInterfaceDevice.currentDevice().systemVersion}",
        device = deviceName,
    )
    initKmm(createNativeDriver(), deviceData)
}

actual fun getResourceContent(file: String, type: String): String {
    TODO("WatchOS getResourceContent() not implemented")
}

internal actual object SecureLocalStorage {

    actual fun getOrNull(key: SecureLocalStorage__Key): String? {
        TODO("WatchOS SecureLocalStorage.getOrNull() not implemented")
    }

    actual fun upsert(key: SecureLocalStorage__Key, value: String?) {
        TODO("WatchOS SecureLocalStorage.upsert() not implemented")
    }
}
