package timeto.shared.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dbsq.ChecklistItemSQ
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import timeto.shared.*

data class ChecklistItemModel(
    val id: Int,
    val text: String,
    val list_id: Int,
    val check_time: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow() = db.checklistItemQueries.anyChange().asFlow()

        suspend fun getAsc() = dbIO {
            db.checklistItemQueries.getAsc().executeAsList().map { it.toModel() }
        }

        fun getAscFlow() = db.checklistItemQueries.getAsc().asFlow()
            .mapToList().map { list -> list.map { it.toModel() } }

        suspend fun addWithValidation(
            text: String,
            checklist: ChecklistModel,
        ) {
            val timeId = time()
            val nextId = if (getAsc().any { it.id == timeId })
                timeId + 1 // todo test
            else
                timeId

            db.checklistItemQueries.insert(
                id = nextId,
                text = validateText(text),
                list_id = checklist.id,
                check_time = 0,
            )
        }

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.checklistItemQueries.getAsc().executeAsList().map { it.toModel() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.checklistItemQueries.insert(
                id = j.getInt(0),
                text = j.getString(1),
                list_id = j.getInt(2),
                check_time = j.getInt(3),
            )
        }
    }

    fun isChecked() = check_time > 0

    suspend fun toggle(): Unit = dbIO {
        db.checklistItemQueries.upCheckTimeById(
            id = id, check_time = if (isChecked()) 0 else time()
        )
    }

    suspend fun upTextWithValidation(newText: String): Unit = dbIO {
        db.checklistItemQueries.upTextById(
            id = id, text = validateText(newText)
        )
    }

    suspend fun delete() = dbIO { db.checklistItemQueries.deleteById(id) }

    ///
    /// Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, text, list_id, check_time
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.checklistItemQueries.upById(
            id = j.getInt(0),
            text = j.getString(1),
            list_id = j.getInt(2),
            check_time = j.getInt(3),
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

private fun ChecklistItemSQ.toModel() = ChecklistItemModel(
    id = id, text = text, list_id = list_id, check_time = check_time
)
