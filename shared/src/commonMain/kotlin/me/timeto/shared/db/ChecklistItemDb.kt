package me.timeto.shared.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import dbsq.ChecklistItemSQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*

data class ChecklistItemDb(
    val id: Int,
    val text: String,
    val list_id: Int,
    val check_time: Int,
    val sort: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow(): Flow<Query<Int>> =
            db.checklistItemQueries.anyChange().asFlow()

        suspend fun selectSorted(): List<ChecklistItemDb> = dbIo {
            db.checklistItemQueries.getSorted().asList { toDb() }
        }

        fun selectSortedFlow(): Flow<List<ChecklistItemDb>> =
            db.checklistItemQueries.getSorted().asListFlow { toDb() }

        suspend fun addWithValidation(
            text: String,
            checklist: ChecklistDb,
        ) {
            val allSorted = selectSorted()

            val timeId = time()
            val nextId = if (allSorted.any { it.id == timeId })
                timeId + 1 // todo test
            else
                timeId

            val sort = allSorted.maxOfOrNull { it.sort }?.plus(1) ?: 0

            db.checklistItemQueries.insert(
                id = nextId,
                text = validateText(text),
                list_id = checklist.id,
                check_time = 0,
                sort = sort,
            )
        }

        suspend fun toggleByList(
            list: ChecklistDb,
            checkOrUncheck: Boolean
        ) = dbIo {
            db.checklistItemQueries.upCheckTimeByList(
                check_time = if (checkOrUncheck) time() else 0,
                list_id = list.id
            )
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.checklistItemQueries.getSorted().executeAsList().map { it.toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.checklistItemQueries.insert(
                id = j.getInt(0),
                text = j.getString(1),
                list_id = j.getInt(2),
                check_time = j.getInt(3),
                sort = j.getInt(4),
            )
        }
    }

    val isChecked = check_time > 0

    suspend fun toggle(): Unit = dbIo {
        db.checklistItemQueries.upCheckTimeById(
            id = id, check_time = if (isChecked) 0 else time()
        )
    }

    suspend fun upTextWithValidation(newText: String): Unit = dbIo {
        db.checklistItemQueries.upTextById(
            id = id, text = validateText(newText)
        )
    }

    suspend fun upSort(newSort: Int): Unit = dbIo {
        db.checklistItemQueries.upSortById(
            id = id,
            sort = newSort,
        )
    }

    suspend fun delete() = dbIo { db.checklistItemQueries.deleteById(id) }

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, text, list_id, check_time, sort,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.checklistItemQueries.upById(
            id = j.getInt(0),
            text = j.getString(1),
            list_id = j.getInt(2),
            check_time = j.getInt(3),
            sort = j.getInt(4),
        )
    }

    override fun backupable__delete() {
        db.checklistItemQueries.deleteById(id)
    }
}

private fun validateText(text: String): String {
    val validatedText = text.trim()
    if (validatedText.isEmpty())
        throw UIException("Empty text")
    return validatedText
}

private fun ChecklistItemSQ.toDb() = ChecklistItemDb(
    id = id, text = text, list_id = list_id,
    check_time = check_time, sort = sort,
)
