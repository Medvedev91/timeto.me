package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.ChecklistItemSQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*
import me.timeto.shared.misc.getInt
import me.timeto.shared.misc.getString
import me.timeto.shared.misc.time
import me.timeto.shared.misc.toJsonArray
import me.timeto.shared.ui.UiException
import kotlin.coroutines.cancellation.CancellationException

data class ChecklistItemDb(
    val id: Int,
    val text: String,
    val list_id: Int,
    val check_time: Int,
    val sort: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow(): Flow<*> =
            db.checklistItemQueries.anyChange().asFlow()

        suspend fun selectSorted(): List<ChecklistItemDb> = dbIo {
            db.checklistItemQueries.getSorted().asList { toDb() }
        }

        fun selectSortedFlow(): Flow<List<ChecklistItemDb>> =
            db.checklistItemQueries.getSorted().asListFlow { toDb() }

        @Throws(UiException::class, CancellationException::class)
        suspend fun insertWithValidation(
            text: String,
            checklist: ChecklistDb,
        ): Unit = dbIo {

            db.transaction {

                val allSorted: List<ChecklistItemDb> =
                    db.checklistItemQueries.getSorted().asList { toDb() }

                val timeId = time()
                val nextId = if (allSorted.any { it.id == timeId })
                    timeId + 1 // todo test
                else
                    timeId

                val sort: Int = allSorted.maxOfOrNull { it.sort }?.plus(1) ?: 0

                val textValidated: String = validateTextRaw(text)

                db.checklistItemQueries.insert(
                    id = nextId,
                    text = textValidated,
                    list_id = checklist.id,
                    check_time = 0,
                    sort = sort,
                )
            }
        }

        suspend fun toggleByList(
            list: ChecklistDb,
            checkOrUncheck: Boolean
        ): Unit = dbIo {
            db.checklistItemQueries.updateCheckTimeByList(
                check_time = if (checkOrUncheck) time() else 0,
                list_id = list.id
            )
        }

        suspend fun updateSortMany(
            itemsDb: List<ChecklistItemDb>,
        ): Unit = dbIo {
            db.transaction {
                itemsDb.forEachIndexed { idx, itemDb ->
                    db.checklistItemQueries.updateSortById(
                        id = itemDb.id,
                        sort = idx,
                    )
                }
            }
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.checklistItemQueries.getSorted().asList { toDb() }

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

    val isChecked: Boolean = check_time > 0

    suspend fun toggle(): Unit = dbIo {
        db.checklistItemQueries.updateCheckTimeById(
            id = id,
            check_time = if (isChecked) 0 else time(),
        )
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateTextWithValidation(newText: String): Unit = dbIo {
        db.checklistItemQueries.updateTextById(
            id = id,
            text = validateTextRaw(newText),
        )
    }

    suspend fun updateSort(newSort: Int): Unit = dbIo {
        db.checklistItemQueries.updateSortById(
            id = id,
            sort = newSort,
        )
    }

    suspend fun delete(): Unit = dbIo {
        db.checklistItemQueries.deleteById(id)
    }

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, text, list_id, check_time, sort,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.checklistItemQueries.updateById(
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

///

@Throws(UiException::class)
private fun validateTextRaw(text: String): String {
    val validatedText = text.trim()
    if (validatedText.isEmpty())
        throw UiException("Empty text")
    return validatedText
}

///

private fun ChecklistItemSQ.toDb() = ChecklistItemDb(
    id = id, text = text, list_id = list_id,
    check_time = check_time, sort = sort,
)
