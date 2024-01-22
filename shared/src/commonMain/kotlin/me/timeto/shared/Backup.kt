package me.timeto.shared

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
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

            "activities" to ActivityDb.getAscSorted().modelsToJsonArray(),
            "intervals" to IntervalModel.getDesc(intervalsLimit).modelsToJsonArray(),
            "task_folders" to TaskFolderModel.getAscBySort().modelsToJsonArray(),
            "tasks" to TaskModel.getAsc().modelsToJsonArray(),
            "checklists" to ChecklistModel.getAsc().modelsToJsonArray(),
            "checklist_items" to ChecklistItemDb.getAsc().modelsToJsonArray(),
            "shortcuts" to ShortcutModel.getAsc().modelsToJsonArray(),
            "repeatings" to RepeatingModel.getAsc().modelsToJsonArray(),
            "events" to EventModel.getAscByTime().modelsToJsonArray(),
            "event_templates" to EventTemplateDB.selectAscSorted().modelsToJsonArray(),
            "notes" to NoteModel.getAsc().modelsToJsonArray(),
            "kv" to KvDb.getAll().modelsToJsonArray(),
        )
        return JsonObject(map).toString()
    }

    @Throws(Exception::class)
    fun restore(jString: String) {
        db.transaction {
            //
            // TRICK Do not use coroutines inside, it crashes transaction.

            val json = Json.parseToJsonElement(jString)

            db.taskQueries.truncate()
            db.taskFolderQueries.truncate()
            db.intervalQueries.truncate()
            db.activityQueries.truncate()
            db.eventQueries.truncate()
            db.eventTemplateQueries.truncate()
            db.repeatingQueries.truncate()
            db.checklistItemQueries.truncate()
            db.checklistQueries.truncate()
            db.shortcutQueries.truncate()
            db.noteQueries.truncate()
            db.kVQueries.truncate()

            json.mapJsonArray("activities") { ActivityDb.backupable__restore(it) }
            json.mapJsonArray("intervals") { IntervalModel.backupable__restore(it) }
            json.mapJsonArray("task_folders") { TaskFolderModel.backupable__restore(it) }
            json.mapJsonArray("tasks") { TaskModel.backupable__restore(it) }
            json.mapJsonArray("checklists") { ChecklistModel.backupable__restore(it) }
            json.mapJsonArray("checklist_items") { ChecklistItemDb.backupable__restore(it) }
            json.mapJsonArray("shortcuts") { ShortcutModel.backupable__restore(it) }
            json.mapJsonArray("repeatings") { RepeatingModel.backupable__restore(it) }
            json.mapJsonArray("events") { EventModel.backupable__restore(it) }
            json.mapJsonArray("event_templates") { EventTemplateDB.backupable__restore(it) }
            json.mapJsonArray("notes") { NoteModel.backupable__restore(it) }
            json.mapJsonArray("kv") { KvDb.backupable__restore(it) }
        }
    }

    fun prepFileName(unixTime: UnixTime, prefix: String): String {
        val year = unixTime.year().toString()
        val month = unixTime.month().toString().padStart(2, '0')
        val day = unixTime.dayOfMonth().toString().padStart(2, '0')
        val (h, m, s) = (unixTime.utcTime() % 86_400).toHms()
            .map { it.toString().padStart(2, '0') }
        return "${prefix}${year}_${month}_${day}_${h}_${m}_${s}.json"
    }

    @Throws(Exception::class)
    fun fileNameToUnixTime(fileName: String): UnixTime {
        val regex = "(\\d{4})_(\\d{2})_(\\d{2})_(\\d{2})_(\\d{2})_(\\d{2})\\.json$".toRegex()
        val match = regex.find(fileName) ?: throw Exception("Backup.fileNameToUnixTime($fileName)")
        val v = match.groupValues.drop(1).map { it.toInt() }
        val dateTime = LocalDateTime(v[0], v[1], v[2], v[3], v[4], v[5], 0)
        val time = dateTime.toInstant(TimeZone.currentSystemDefault()).epochSeconds.toInt()
        return UnixTime(time)
    }
}

private inline fun JsonElement.mapJsonArray(
    key: String,
    block: (JsonArray) -> Unit,
) {
    this.jsonObject[key]!!.jsonArray.forEach { block(it.jsonArray) }
}

private fun List<Backupable__Item>.modelsToJsonArray() =
    JsonArray(this.map { it.backupable__backup() })
