package me.timeto.shared

import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.JournalMode
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.squareup.sqldelight.drivers.native.wrapConnection
import platform.Foundation.NSBundle
import platform.WatchKit.WKInterfaceDevice
import timeto.dbsq.TimetoDB
import me.timeto.shared.db.DB_NAME

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

//////

/**
 * WARNING
 * DO NOT CHANGE THE CODE! It is copy from "iosMain".
 */
private fun createNativeDriver(
    schema: SqlDriver.Schema = TimetoDB.Schema,
) = NativeSqliteDriver(
    configuration = DatabaseConfiguration(
        name = DB_NAME,
        version = schema.version,
        create = { connection ->
            wrapConnection(connection) { schema.create(it) }
        },
        upgrade = { connection, oldVersion, newVersion ->
            wrapConnection(connection) { schema.migrate(it, oldVersion, newVersion) }
        },
        journalMode = JournalMode.DELETE, // Changed from JournalMode.WAL
    )
)
