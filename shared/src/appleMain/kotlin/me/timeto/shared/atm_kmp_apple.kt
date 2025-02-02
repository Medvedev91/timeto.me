package me.timeto.shared

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.JournalMode
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import platform.Foundation.*
import platform.posix.uname
import platform.posix.utsname


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

// Based on https://slack-chats.kotlinlang.org/t/527926
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal fun machineIdentifier(): String {
    memScoped {
        val q = alloc<utsname>()
        uname(q.ptr)
        return NSString.stringWithCString(q.machine, encoding = NSUTF8StringEncoding) ?: ""
    }
}

//
// Swift Flow

class SwiftFlow<T>(kotlinFlow: Flow<T>) : Flow<T> by kotlinFlow {
    fun watch(block: (T) -> Unit): SwiftFlow__Cancellable {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        onEach(block).launchIn(scope)
        return object : SwiftFlow__Cancellable {
            override fun cancel() {
                scope.cancel()
            }
        }
    }
}

interface SwiftFlow__Cancellable {
    fun cancel()
}
