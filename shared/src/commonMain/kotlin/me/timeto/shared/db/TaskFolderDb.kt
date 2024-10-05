package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dbsq.TaskFolderSQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*

data class TaskFolderDb(
    val id: Int,
    val name: String,
    val sort: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        const val ID_TODAY = 1
        const val ID_TMRW = 4

        fun anyChangeFlow() = db.taskFolderQueries.anyChange().asFlow()

        suspend fun selectAllSorted(): List<TaskFolderDb> = dbIo {
            db.taskFolderQueries.getAscBySort().executeAsList().map { it.toDb() }
        }

        fun getAscBySortFlow() = db.taskFolderQueries.getAscBySort().asFlow()
            .mapToList(Dispatchers.IO).map { list -> list.map { it.toDb() } }

        //////

        suspend fun addTmrw() = dbIo { addRaw(id = ID_TMRW, name = "TMRW", sort = 2) }

        suspend fun addWithValidation(name: String) = dbIo {
            addRaw(
                id = time(),
                name = validateName(name),
                sort = selectAllSorted().maxOf { it.sort } + 1,
            )
        }

        fun addRaw(
            id: Int,
            name: String,
            sort: Int,
        ) {
            db.taskFolderQueries.insert(
                id = id, name = name, sort = sort
            )
        }

        fun List<TaskFolderDb>.sortedFolders() = this.sortedWith(
            compareBy({ it.sort }, { it.id })
        )

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.taskFolderQueries.getAscBySort().executeAsList().map { it.toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.taskFolderQueries.insert(
                id = j.getInt(0),
                name = j.getString(1),
                sort = j.getInt(2),
            )
        }
    }

    val isToday = id == ID_TODAY
    val isTmrw = id == ID_TMRW

    fun upNameWithValidation(newName: String) {
        db.taskFolderQueries.upNameById(id = id, name = validateName(newName))
    }

    fun upSort(newSort: Int) {
        db.taskFolderQueries.upSortById(id = id, sort = newSort)
    }

    ///
    /// Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, name, sort
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.taskFolderQueries.upById(
            id = j.getInt(0),
            name = j.getString(1),
            sort = j.getInt(2),
        )
    }

    override fun backupable__delete() {
        db.taskFolderQueries.deleteById(id)
    }
}

private fun validateName(name: String): String {
    val validatedName = name.trim()
    if (validatedName.isBlank())
        throw UIException("Invalid folder name")
    return validatedName
}

private fun TaskFolderSQ.toDb() = TaskFolderDb(
    id = id, name = name, sort = sort,
)
