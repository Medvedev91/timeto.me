package me.timeto.shared

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.JournalMode
import platform.Foundation.NSBundle
import platform.WatchKit.WKInterfaceDevice
import me.timeto.appdbsq.TimetomeDB
import me.timeto.shared.db.DB_NAME
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

internal actual val REPORT_API_TITLE = "⌚ Watch OS"

fun initKmmWatchOS(deviceName: String) {
    val deviceData = DeviceData(
        build = (NSBundle.mainBundle.infoDictionary!!["CFBundleVersion"] as String).toInt(),
        os = "watchos-${WKInterfaceDevice.currentDevice().systemVersion}",
        device = deviceName,
    )
    initKmm(createNativeDriver(), deviceData)
}

actual fun time(): Int = NSDate().timeIntervalSince1970.toInt()

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
    schema: SqlSchema<QueryResult.Value<Unit>> = TimetomeDB.Schema,
) = NativeSqliteDriver(
    configuration = DatabaseConfiguration(
        name = DB_NAME,
        version = schema.version.toInt(),
        create = { connection ->
            wrapConnection(connection) { schema.create(it) }
        },
        upgrade = { connection, oldVersion, newVersion ->
            wrapConnection(connection) { schema.migrate(it, oldVersion.toLong(), newVersion.toLong()) }
        },
        journalMode = JournalMode.DELETE, // Changed from JournalMode.WAL
    )
)
