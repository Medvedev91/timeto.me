package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dbsq.ChecklistSQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*

data class ChecklistDb(
    val id: Int,
    val name: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow() = db.checklistQueries.anyChange().asFlow()

        suspend fun getAsc() = dbIO {
            db.checklistQueries.getAsc().executeAsList().map { it.toModel() }
        }

        fun getAscFlow() = db.checklistQueries.getAsc().asFlow()
            .mapToList(Dispatchers.IO).map { list -> list.map { it.toModel() } }

        suspend fun addWithValidation(
            name: String,
        ) = dbIO {
            val nextId = time()
            db.checklistQueries.insert(
                id = nextId,
                name = validateName(name),
            )
        }

        private suspend fun validateName(
            name: String,
            exIds: Set<Int> = setOf(),
        ): String {

            val validatedName = name.trim()
            if (validatedName.isEmpty())
                throw UIException("Empty name")

            getAsc()
                .filter { it.id !in exIds }
                .forEach { checklist ->
                    if (checklist.name.equals(validatedName, ignoreCase = true))
                        throw UIException("$validatedName already exists.")
                }

            return validatedName
        }

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.checklistQueries.getAsc().executeAsList().map { it.toModel() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.checklistQueries.insert(
                id = j.getInt(0),
                name = j.getString(1),
            )
        }
    }

    fun performUI() {
        launchExDefault { uiChecklistFlow.emit(this@ChecklistDb) }
    }

    suspend fun upNameWithValidation(newName: String): Unit = dbIO {
        db.checklistQueries.upNameById(
            id = id, name = validateName(newName, setOf(id))
        )
    }

    suspend fun deleteWithDependencies(): Unit = dbIO {
        ChecklistItemDb.getAsc().filter { it.list_id == id }.forEach { it.delete() }
        db.checklistQueries.deleteById(id)
    }

    ///
    /// Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, name
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.checklistQueries.upNameById(
            id = j.getInt(0),
            name = j.getString(1),
        )
    }

    override fun backupable__delete() {
        db.checklistQueries.deleteById(id)
    }
}

private fun ChecklistSQ.toModel() = ChecklistDb(
    id = id, name = name
)
