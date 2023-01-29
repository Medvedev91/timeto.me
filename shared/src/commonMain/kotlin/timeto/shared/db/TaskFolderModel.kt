package timeto.shared.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dbsq.TaskFolderSQ
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import timeto.shared.*

data class TaskFolderModel(
    val id: Int,
    val name: String,
    val sort: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        const val ID_TODAY = 1
        const val ID_WEEK = 2
        const val ID_INBOX = 3

        fun anyChangeFlow() = db.taskFolderQueries.anyChange().asFlow()

        suspend fun getAscBySort() = dbIO {
            db.taskFolderQueries.getAscBySort().executeAsList().map { it.toModel() }
        }

        fun getAscBySortFlow() = db.taskFolderQueries.getAscBySort().asFlow()
            .mapToList().map { list -> list.map { it.toModel() } }

        ///
        /// Getsert

        fun getToday() = getById(ID_TODAY)

        fun getWeek() = getById(ID_WEEK)

        fun getInbox() = getById(ID_INBOX)

        fun getById(id: Int) = DI.taskFolders.first { it.id == id }

        //////

        fun addRaw(
            id: Int,
            name: String,
            sort: Int,
        ) {
            db.taskFolderQueries.insert(
                id = id, name = name, sort = sort
            )
        }

        private fun TaskFolderSQ.toModel() = TaskFolderModel(
            id = id, name = name, sort = sort
        )

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.taskFolderQueries.getAscBySort().executeAsList().map { it.toModel() }

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
    val isWeek = id == ID_WEEK
    val isInbox = id == ID_INBOX

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
