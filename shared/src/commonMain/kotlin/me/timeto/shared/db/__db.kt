package me.timeto.shared.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timeto.dbsq.TimetoDB

internal const val DB_NAME = "timeto.db"
internal lateinit var db: TimetoDB

internal suspend fun <T> dbIO(
    block: suspend CoroutineScope.() -> T
): T = withContext(Dispatchers.Default, block)
