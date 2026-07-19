package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.TaskFolderSq
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.Cache
import me.timeto.shared.Symbol
import me.timeto.shared.Symbol.Icon
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getString
import me.timeto.shared.time
import me.timeto.shared.toJsonArray
import me.timeto.shared.UiException
import me.timeto.shared.getIntOrNull
import me.timeto.shared.textFeatures
import kotlin.coroutines.cancellation.CancellationException

data class TaskFolderDb(
    val id: Int,
    val sort: Int,
    val activity_id: Int?,
    val name: String,
    val symbol_raw: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        const val ID_TODAY = 1
        const val ID_TOMORROW = 4
        const val ID_SOMEDAY = 5

        fun anyChangeFlow(): Flow<*> =
            db.taskFolderQueries.anyChange().asFlow()

        suspend fun selectAllSorted(): List<TaskFolderDb> = dbIo {
            db.taskFolderQueries.selectAllSorted().asList { toDb() }.uiSorted()
        }

        fun selectAllSortedFlow(): Flow<List<TaskFolderDb>> =
            db.taskFolderQueries.selectAllSorted().asListFlow { toDb() }.map { it.uiSorted() }

        ///

        @Throws(UiException::class, CancellationException::class)
        suspend fun insertWithValidation(
            rawName: String,
            activityDb: ActivityDb?,
            symbol: Symbol,
        ) = dbIo {
            db.transaction {
                val allTaskFoldersDb: List<TaskFolderDb> =
                    db.taskFolderQueries.selectAllSorted().asList { toDb() }
                if (activityDb != null && allTaskFoldersDb.any { activityDb.id == it.activity_id })
                    throw UiException("${activityDb.name.textFeatures().textNoFeatures} already exists")
                db.taskFolderQueries.insert(
                    id = time(),
                    sort = allTaskFoldersDb.maxOf { it.sort } + 1,
                    activity_id = activityDb?.id,
                    name = validateName(rawName),
                    symbol_raw = symbol.raw,
                )
            }
        }

        suspend fun insertNoValidation(
            id: Int,
            sort: Int,
            activityDb: ActivityDb?,
            name: String,
            symbol: Symbol,
        ): Unit = dbIo {
            db.taskFolderQueries.insert(
                id = id,
                sort = sort,
                activity_id = activityDb?.id,
                name = name,
                symbol_raw = symbol.raw,
            )
        }

        suspend fun updateSortMany(
            foldersDb: List<TaskFolderDb>,
        ): Unit = dbIo {
            db.transaction {
                foldersDb.forEachIndexed { idx, folderDb ->
                    db.taskFolderQueries.updateSortById(
                        id = folderDb.id,
                        sort = idx,
                    )
                }
            }
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.taskFolderQueries.selectAllSorted().asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.taskFolderQueries.insert(
                id = j.getInt(0),
                sort = j.getInt(1),
                activity_id = j.getIntOrNull(2),
                name = j.getString(3),
                symbol_raw = j.getString(4),
            )
        }
    }

    ///

    val isToday: Boolean =
        id == ID_TODAY

    val isTomorrow: Boolean =
        id == ID_TOMORROW

    val isSomeday: Boolean =
        id == ID_SOMEDAY

    fun symbolOrDefault(): Symbol =
        Symbol.fromRawOrNull(symbol_raw) ?: Icon.IconEnum.inbox.toIcon()

    fun selectActivityDbOrNullCached(): ActivityDb? {
        if (activity_id == null)
            return null
        return Cache.activitiesDb.first { activity_id == it.id }
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateWithValidation(
        sort: Int,
        activityDb: ActivityDb?,
        rawName: String,
        symbol: Symbol,
    ): Unit = dbIo {
        db.transaction {
            if (activityDb != null) {
                val isExists: Boolean = db.taskFolderQueries.selectAllSorted()
                    .asList { toDb() }
                    .filter { it.id != id }
                    .any { it.activity_id == activityDb.id }
                if (isExists)
                    throw UiException("${activityDb.name.textFeatures().textNoFeatures} already exists")
            }
            db.taskFolderQueries.updateById(
                id = id,
                sort = sort,
                activity_id = activityDb?.id,
                name = validateName(rawName),
                symbol_raw = symbol.raw,
            )
        }
    }

    suspend fun updateSort(newSort: Int): Unit = dbIo {
        db.taskFolderQueries.updateSortById(id = id, sort = newSort)
    }

    suspend fun delete(): Unit = dbIo {
        db.taskFolderQueries.deleteById(id)
    }

    //
    // Backupable Item

    override fun backupable__getId(): String =
        id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, sort, activity_id, name, symbol_raw,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.taskFolderQueries.updateById(
            id = j.getInt(0),
            sort = j.getInt(1),
            activity_id = j.getIntOrNull(2),
            name = j.getString(3),
            symbol_raw = j.getString(4),
        )
    }

    override fun backupable__delete() {
        db.taskFolderQueries.deleteById(id)
    }
}

///

@Throws(UiException::class)
private fun validateName(name: String): String {
    val validatedName = name.trim()
    if (validatedName.isBlank())
        throw UiException("Invalid folder name")
    return validatedName
}

///

private fun TaskFolderSq.toDb() = TaskFolderDb(
    id = id,
    sort = sort,
    activity_id = activity_id,
    name = name,
    symbol_raw = symbol_raw,
)

private fun List<TaskFolderDb>.uiSorted(): List<TaskFolderDb> =
    sortedWith(compareBy({ it.sort }, { it.id }))
