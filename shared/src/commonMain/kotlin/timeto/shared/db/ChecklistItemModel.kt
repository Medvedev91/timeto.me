package timeto.shared.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dbsq.ChecklistItemSQ
import kotlinx.coroutines.flow.map
import timeto.shared.UIException
import timeto.shared.time

data class ChecklistItemModel(
    val id: Int,
    val text: String,
    val list_id: Int,
    val check_time: Int,
) {

    companion object {

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

            addRaw(
                id = nextId,
                text = validateText(text),
                listId = checklist.id,
                checkTime = 0
            )
        }

        fun addRaw(
            id: Int,
            text: String,
            listId: Int,
            checkTime: Int,
        ) {
            db.checklistItemQueries.insert(
                id = id, text = text, list_id = listId, check_time = checkTime
            )
        }

        fun truncate() {
            db.checklistItemQueries.truncate()
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
}
