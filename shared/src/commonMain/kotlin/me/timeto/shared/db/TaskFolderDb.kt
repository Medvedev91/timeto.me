package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.TaskFolderSQ
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getString
import me.timeto.shared.time
import me.timeto.shared.toJsonArray
import me.timeto.shared.UiException
import kotlin.coroutines.cancellation.CancellationException

data class TaskFolderDb(
    val id: Int,
    val name: String,
    val sort: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        const val ID_TODAY = 1
        const val ID_TMRW = 4

        fun anyChangeFlow(): Flow<*> =
            db.taskFolderQueries.anyChange().asFlow()

        suspend fun selectAllSorted(): List<TaskFolderDb> = dbIo {
            db.taskFolderQueries.selectAllSorted().asList { toDb() }.uiSorted()
        }

        fun selectAllSortedFlow(): Flow<List<TaskFolderDb>> =
            db.taskFolderQueries.selectAllSorted().asListFlow { toDb() }.map { it.uiSorted() }

        ///

        suspend fun insertTmrw(): Unit = dbIo {
            db.taskFolderQueries.insert(
                id = ID_TMRW,
                name = "TMRW",
                sort = 2,
            )
        }

        @Throws(UiException::class, CancellationException::class)
        suspend fun insertWithValidation(name: String) = dbIo {
            db.transaction {
                val allTaskFoldersDb: List<TaskFolderDb> =
                    db.taskFolderQueries.selectAllSorted().asList { toDb() }
                db.taskFolderQueries.insert(
                    id = time(),
                    name = validateName(name),
                    sort = allTaskFoldersDb.maxOf { it.sort } + 1,
                )
            }
        }

        suspend fun insertNoValidation(
            id: Int,
            name: String,
            sort: Int,
        ): Unit = dbIo {
            db.taskFolderQueries.insert(
                id = id, name = name, sort = sort
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
                name = j.getString(1),
                sort = j.getInt(2),
            )
        }
    }

    ///

    val isToday: Boolean = id == ID_TODAY
    val isTmrw: Boolean = id == ID_TMRW

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateNameWithValidation(newName: String): Unit = dbIo {
        db.taskFolderQueries.updateNameById(id = id, name = validateName(newName))
    }

    suspend fun updateSort(newSort: Int): Unit = dbIo {
        db.taskFolderQueries.updateSortById(id = id, sort = newSort)
    }

    suspend fun delete(): Unit = dbIo {
        db.taskFolderQueries.deleteById(id)
    }

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, name, sort,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.taskFolderQueries.updateById(
            id = j.getInt(0),
            name = j.getString(1),
            sort = j.getInt(2),
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

private fun TaskFolderSQ.toDb() = TaskFolderDb(
    id = id, name = name, sort = sort,
)

private fun List<TaskFolderDb>.uiSorted(): List<TaskFolderDb> =
    sortedWith(compareBy({ it.sort }, { it.id }))
