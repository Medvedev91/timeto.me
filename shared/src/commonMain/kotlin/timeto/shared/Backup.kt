package timeto.shared

import kotlinx.serialization.json.*
import timeto.shared.db.*

object Backup {

    suspend fun create(
        type: String,
        intervalsLimit: Int = Int.MAX_VALUE,
    ): String {
        val map: Map<String, JsonElement> = mapOf(
            "version" to JsonPrimitive(1),
            "type" to JsonPrimitive(type),

            "activities" to ActivityModel.getAscSorted().modelsToJsonArray { it.backupable__backup() },
            "intervals" to IntervalModel.getDesc(intervalsLimit).modelsToJsonArray { it.backupable__backup() },
            "task_folders" to TaskFolderModel.getAscBySort().modelsToJsonArray { it.backupable__backup() },
            "tasks" to TaskModel.getAsc().modelsToJsonArray { it.backupable__backup() },
            "checklists" to ChecklistModel.getAsc().modelsToJsonArray { it.backupable__backup() },
            "checklist_items" to ChecklistItemModel.getAsc().modelsToJsonArray { it.backupable__backup() },
            "shortcuts" to ShortcutModel.getAsc().modelsToJsonArray { it.backupable__backup() },
            "repeatings" to RepeatingModel.getAsc().modelsToJsonArray { it.backupable__backup() },

            "events" to EventModel.getAscByTime().modelsToJsonArray { c ->
                listOf(c.id, c.utc_time, c.text).toJsonArray()
            },
            "kv" to KVModel.getAll().modelsToJsonArray { i ->
                listOf(i.key, i.value).toJsonArray()
            },
        )
        return JsonObject(map).toString()
    }

    fun restore(jString: String) {
        db.transaction {
            restoreV1NeedTransaction(jString)
        }
    }
}

/**
 * WARNING
 * Do not use coroutines inside, it crashes transaction.
 */
private fun restoreV1NeedTransaction(jString: String) {
    val json = Json.parseToJsonElement(jString)

    db.taskQueries.truncate()
    db.taskFolderQueries.truncate()
    db.intervalQueries.truncate()
    db.activityQueries.truncate()
    db.eventQueries.truncate()
    db.repeatingQueries.truncate()
    db.checklistItemQueries.truncate()
    db.checklistQueries.truncate()
    db.shortcutQueries.truncate()
    db.kVQueries.truncate()

    json.mapJsonArray("activities") { ActivityModel.backupable__restore(it) }
    json.mapJsonArray("intervals") { IntervalModel.backupable__restore(it) }
    json.mapJsonArray("task_folders") { TaskFolderModel.backupable__restore(it) }
    json.mapJsonArray("tasks") { TaskModel.backupable__restore(it) }
    json.mapJsonArray("checklists") { ChecklistModel.backupable__restore(it) }
    json.mapJsonArray("checklist_items") { ChecklistItemModel.backupable__restore(it) }
    json.mapJsonArray("shortcuts") { ShortcutModel.backupable__restore(it) }
    json.mapJsonArray("repeatings") { RepeatingModel.backupable__restore(it) }

    json.mapJsonArray("events") { j ->
        EventModel.addRaw(
            id = j.getInt(0),
            text = j.getString(2),
            utcTime = j.getInt(1),
        )
    }

    json.mapJsonArray("kv") { j ->
        KVModel.addRaw(
            k = j.getString(0),
            v = j.getString(1),
        )
    }
}

private inline fun JsonElement.mapJsonArray(
    key: String,
    block: (JsonArray) -> Unit,
) {
    this.jsonObject[key]!!.jsonArray.forEach { block(it.jsonArray) }
}

private inline fun <T> List<T>.modelsToJsonArray(
    block: (T) -> JsonElement,
) = JsonArray(this.map { block(it) })
