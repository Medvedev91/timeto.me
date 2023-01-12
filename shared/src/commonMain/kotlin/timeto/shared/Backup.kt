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

            "events" to EventModel.getAscByTime().modelsToJsonArray { c ->
                listOf(c.id, c.utc_time, c.text).toJsonArray()
            },
            "repeatings" to RepeatingModel.getAsc().modelsToJsonArray { r ->
                listOf(r.id, r.text, r.last_day, r.type_id, r.value).toJsonArray()
            },
            "checklists" to ChecklistModel.getAsc().modelsToJsonArray { i ->
                listOf(i.id, i.name).toJsonArray()
            },
            "checklist_items" to ChecklistItemModel.getAsc().modelsToJsonArray { i ->
                listOf(i.id, i.text, i.list_id, i.check_time).toJsonArray()
            },
            "shortcuts" to ShortcutModel.getAsc().modelsToJsonArray { i ->
                listOf(i.id, i.name, i.uri).toJsonArray()
            },
            "kv" to KVModel.getAll().modelsToJsonArray { i ->
                listOf(i.key, i.value).toJsonArray()
            },
        )
        return JsonObject(map).toString()
    }

    suspend fun restore(jString: String) {
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

    TaskModel.truncate()
    TaskFolderModel.truncate()
    IntervalModel.truncate()
    ActivityModel.truncate()
    EventModel.truncate()
    RepeatingModel.truncate()
    ChecklistItemModel.truncate()
    ChecklistModel.truncate()
    ShortcutModel.truncate()
    KVModel.truncate()

    json.mapJsonArray("activities") { ActivityModel.backupable__restore(it) }
    json.mapJsonArray("intervals") { IntervalModel.backupable__restore(it) }
    json.mapJsonArray("task_folders") { TaskFolderModel.backupable__restore(it) }
    json.mapJsonArray("tasks") { TaskModel.backupable__restore(it) }

    json.mapJsonArray("events") { j ->
        EventModel.addRaw(
            id = j.getInt(0),
            text = j.getString(2),
            utcTime = j.getInt(1),
        )
    }

    json.mapJsonArray("repeatings") { j ->
        RepeatingModel.addRaw(
            id = j.getInt(0),
            text = j.getString(1),
            last_day = j.getInt(2),
            type_id = j.getInt(3),
            value = j.getString(4),
        )
    }

    json.mapJsonArray("checklists") { j ->
        ChecklistModel.addRaw(
            id = j.getInt(0),
            name = j.getString(1),
        )
    }

    json.mapJsonArray("checklist_items") { j ->
        ChecklistItemModel.addRaw(
            id = j.getInt(0),
            text = j.getString(1),
            listId = j.getInt(2),
            checkTime = j.getInt(3),
        )
    }

    json.mapJsonArray("shortcuts") { j ->
        ShortcutModel.addRaw(
            id = j.getInt(0),
            name = j.getString(1),
            uri = j.getString(2),
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
