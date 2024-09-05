package me.timeto.shared.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.timeto.appdbsq.TimetomeDB

internal const val DB_NAME = "timetome.db"
internal lateinit var db: TimetomeDB

internal suspend fun <T> dbIo(
    block: suspend CoroutineScope.() -> T
): T = withContext(Dispatchers.Default, block)
