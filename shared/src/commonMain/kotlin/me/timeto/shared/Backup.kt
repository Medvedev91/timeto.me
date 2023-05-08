package me.timeto.shared

import kotlinx.serialization.json.*
import me.timeto.shared.db.*

object Backup {

    suspend fun create(
        type: String,
        intervalsLimit: Int = Int.MAX_VALUE,
    ): String {
        val map: Map<String, JsonElement> = mapOf(
            "version" to JsonPrimitive(1),
            "type" to JsonPrimitive(type),

            "activities" to ActivityModel.getAscSorted().modelsToJsonArray(),
            "intervals" to IntervalModel.getDesc(intervalsLimit).modelsToJsonArray(),
            "task_folders" to TaskFolderModel.getAscBySort().modelsToJsonArray(),
            "tasks" to TaskModel.getAsc().modelsToJsonArray(),
            "checklists" to ChecklistModel.getAsc().modelsToJsonArray(),
            "checklist_items" to ChecklistItemModel.getAsc().modelsToJsonArray(),
            "shortcuts" to ShortcutModel.getAsc().modelsToJsonArray(),
            "repeatings" to RepeatingModel.getAsc().modelsToJsonArray(),
            "events" to EventModel.getAscByTime().modelsToJsonArray(),
            "kv" to KVModel.getAll().modelsToJsonArray(),
        )
        return JsonObject(map).toString()
    }

    fun restore(jString: String) {
        db.transaction {
            restoreV1NeedTransaction(jString)
        }
    }

    fun prepFileName(unixTime: UnixTime): String {
        val year = unixTime.year().toString()
        val month = unixTime.month().toString().padStart(2, '0')
        val day = unixTime.dayOfMonth().toString().padStart(2, '0')
        val (h, m, s) = (unixTime.utcTime() % 86_400).toHms()
            .map { it.toString().padStart(2, '0') }
        return "${year}_${month}_${day}__${h}_${m}_${s}"
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
    json.mapJsonArray("events") { EventModel.backupable__restore(it) }
    json.mapJsonArray("kv") { KVModel.backupable__restore(it) }
}

private inline fun JsonElement.mapJsonArray(
    key: String,
    block: (JsonArray) -> Unit,
) {
    this.jsonObject[key]!!.jsonArray.forEach { block(it.jsonArray) }
}

private fun List<Backupable__Item>.modelsToJsonArray() =
    JsonArray(this.map { it.backupable__backup() })
