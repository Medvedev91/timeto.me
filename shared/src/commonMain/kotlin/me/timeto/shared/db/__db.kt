package me.timeto.shared.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.timeto.appdbsq.TimetomeDB

internal const val DB_NAME = "timetome.db"
internal lateinit var db: TimetomeDB

internal suspend fun <T> dbIo(
    block: suspend CoroutineScope.() -> T,
): T = withContext(Dispatchers.IO, block)

internal fun <T : Any, R : Any> Query<T>.asList(mapper: T.() -> R): List<R> =
    executeAsList().map(mapper)

internal fun <T : Any, R : Any> Query<T>.asListFlow(mapper: T.() -> R): Flow<List<R>> =
    asFlow().mapToList(Dispatchers.IO).map { list -> list.map { mapper(it) } }
