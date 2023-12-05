package me.timeto.shared

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.JournalMode
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

//
// Time

actual fun time(): Int = NSDate().timeIntervalSince1970.toInt()

actual fun timeMls(): Long = (NSDate().timeIntervalSince1970 * 1_000).toLong()

actual fun getLocalUtcOffset(): Int = TODO()

/**
 * SqlDelight
 *
 * Code copied from the constructor of NativeSqliteDriver(schema, dbName)
 * with adding "journalMode = JournalMode.DELETE". The default was "JournalMode.WAL".
 *
 * Same issue https://github.com/cashapp/sqldelight/issues/2123
 *
 * The issue: if you try to read immediately after writing to the database,
 * you can get the old values. The problem occurs rarely, it is difficult
 * to reproduce, occurs more frequently in release builds. Examples:
 * - I subscribe to the flow, write to the database, in the flow comes
 *   outdated data, such as the last interval, in this case, when you add
 *   a new interval will be set notification for the previous one.
 * - I subscribe to database changes to sync with the Apple Watch,
 *   when the database changes the old data will be sent.
 * - When the database is changed, the irrelevant UI will be displayed.
 */
internal fun createNativeDriver(
    dbName: String,
    schema: SqlSchema<QueryResult.Value<Unit>>,
) = NativeSqliteDriver(
    configuration = DatabaseConfiguration(
        name = dbName,
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
