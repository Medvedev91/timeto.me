package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.ChecklistSQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*
import me.timeto.shared.misc.UiException
import kotlin.coroutines.cancellation.CancellationException

data class ChecklistDb(
    val id: Int,
    val name: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow() = db.checklistQueries.anyChange().asFlow()

        suspend fun selectAsc(): List<ChecklistDb> = dbIo {
            db.checklistQueries.selectAsc().asList { toDb() }
        }

        fun selectAscFlow(): Flow<List<ChecklistDb>> =
            db.checklistQueries.selectAsc().asListFlow { toDb() }

        @Throws(UiException::class, CancellationException::class)
        suspend fun insertWithValidation(
            name: String,
        ): ChecklistDb = dbIo {
            db.transactionWithResult {
                val nextId: Int = time()
                val nameValidated: String =
                    nameValidationRaw(name, exIds = emptySet())
                val sqModel = ChecklistSQ(
                    id = nextId,
                    name = nameValidated,
                )
                db.checklistQueries.insert(sqModel)
                sqModel.toDb()
            }
        }

        //
        // Backupable Holder

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

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, name
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.checklistQueries.updateById(
            id = j.getInt(0),
            name = j.getString(1),
        )
    }

    override fun backupable__delete() {
        db.checklistQueries.deleteById(id)
    }
}

///

private fun ChecklistSQ.toDb() = ChecklistDb(
    id = id, name = name,
)

@Throws(UiException::class)
private fun nameValidationRaw(
    name: String,
    exIds: Set<Int>,
): String {

    val validatedName = name.trim()
    if (validatedName.isEmpty())
        throw UiException("Empty name")

    db.checklistQueries.selectAsc()
        .asList { toDb() }
        .filter { it.id !in exIds }
        .forEach { checklistDb ->
            if (checklistDb.name.equals(validatedName, ignoreCase = true))
                throw UiException("$validatedName already exists.")
        }

    return validatedName
}
