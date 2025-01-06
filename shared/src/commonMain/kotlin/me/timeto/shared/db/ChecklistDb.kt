package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.ChecklistSQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*

data class ChecklistDb(
    val id: Int,
    val name: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow() = db.checklistQueries.anyChange().asFlow()

        suspend fun selectAsc(): List<ChecklistDb> = dbIo {
            db.checklistQueries.selectAsc().executeAsList().map { it.toDb() }
        }

        fun selectAscFlow(): Flow<List<ChecklistDb>> =
            db.checklistQueries.selectAsc().asListFlow { it.toDb() }

        suspend fun addWithValidation(
            name: String,
        ): ChecklistDb = dbIo {
            val nextId = time()
            val sqModel = ChecklistSQ(
                id = nextId,
                name = validateName(name),
            )
            db.checklistQueries.insert(sqModel)
            sqModel.toDb()
        }

        private suspend fun validateName(
            name: String,
            exIds: Set<Int> = setOf(),
        ): String {

            val validatedName = name.trim()
            if (validatedName.isEmpty())
                throw UIException("Empty name")

            selectAsc()
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
            db.checklistQueries.selectAsc().executeAsList().map { it.toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.checklistQueries.insert(
                ChecklistSQ(
                    id = j.getInt(0),
                    name = j.getString(1),
                )
            )
        }
    }

    fun getItemsCached(): List<ChecklistItemDb> =
        Cache.checklistItemsDb.filter { it.list_id == id }

    fun performUI() {
        launchExDefault { uiChecklistFlow.emit(this@ChecklistDb) }
    }

    suspend fun upNameWithValidation(
        newName: String,
    ): ChecklistDb = dbIo {
        val newName = validateName(newName, setOf(id))
        db.checklistQueries.upNameById(
            id = id,
            name = newName,
        )
        this@ChecklistDb.copy(name = newName)
    }

    suspend fun deleteWithDependencies(): Unit = dbIo {
        ChecklistItemDb.getSorted().filter { it.list_id == id }.forEach { it.delete() }
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

private fun ChecklistSQ.toDb() = ChecklistDb(
    id = id, name = name,
)
