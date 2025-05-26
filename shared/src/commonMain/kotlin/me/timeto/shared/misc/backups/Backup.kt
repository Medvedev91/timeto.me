package me.timeto.shared.misc.backups

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.*
import me.timeto.shared.UnixTime
import me.timeto.shared.db.*
import me.timeto.shared.misc.SystemInfo
import me.timeto.shared.toHms

object Backup {

    suspend fun create(
        type: String,
        intervalsLimit: Int = Int.MAX_VALUE,
    ): String {
        val systemInfo = SystemInfo.instance
        val map: Map<String, JsonElement> = mapOf(
            // Meta
            "version" to JsonPrimitive(1),
            "type" to JsonPrimitive(type),
            "time" to JsonPrimitive(UnixTime().time),
            "system" to JsonObject(
                mapOf(
                    "build" to JsonPrimitive(systemInfo.build),
                    "version" to JsonPrimitive(systemInfo.version),
                    "os" to JsonPrimitive(systemInfo.os.fullVersion),
                    "device" to JsonPrimitive(systemInfo.device),
                    "flavor" to JsonPrimitive(systemInfo.flavor),
                )
            ),
            // Data
            "activities" to ActivityDb.selectSorted().modelsToJsonArray(),
            "intervals" to IntervalDb.selectDesc(intervalsLimit).modelsToJsonArray(),
            "goals" to GoalDb.selectAll().modelsToJsonArray(),
            "task_folders" to TaskFolderDb.selectAllSorted().modelsToJsonArray(),
            "tasks" to TaskDb.getAsc().modelsToJsonArray(),
            "checklists" to ChecklistDb.selectAsc().modelsToJsonArray(),
            "checklist_items" to ChecklistItemDb.selectSorted().modelsToJsonArray(),
            "shortcuts" to ShortcutDb.selectAsc().modelsToJsonArray(),
            "repeatings" to RepeatingDb.selectAsc().modelsToJsonArray(),
            "events" to EventDb.getAscByTime().modelsToJsonArray(),
            "event_templates" to EventTemplateDb.selectAscSorted().modelsToJsonArray(),
            "notes" to NoteDb.selectAsc().modelsToJsonArray(),
            "kv" to KvDb.selectAll().modelsToJsonArray(),
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
            db.goalQueries.truncate()
            db.intervalQueries.truncate()
            db.activityQueries.truncate()
            db.eventQueries.truncate()
            db.eventTemplateQueries.truncate()
            db.repeatingQueries.deleteAll()
            db.checklistItemQueries.truncate()
            db.checklistQueries.truncate()
            db.shortcutQueries.truncate()
            db.noteQueries.truncate()
            db.kVQueries.truncate()

            json.mapJsonArray("activities") { ActivityDb.backupable__restore(it) }
            json.mapJsonArray("intervals") { IntervalDb.backupable__restore(it) }
            json.mapJsonArray("goals") { GoalDb.backupable__restore(it) }
            json.mapJsonArray("task_folders") { TaskFolderDb.backupable__restore(it) }
            json.mapJsonArray("tasks") { TaskDb.backupable__restore(it) }
            json.mapJsonArray("checklists") { ChecklistDb.backupable__restore(it) }
            json.mapJsonArray("checklist_items") { ChecklistItemDb.backupable__restore(it) }
            json.mapJsonArray("shortcuts") { ShortcutDb.backupable__restore(it) }
            json.mapJsonArray("repeatings") { RepeatingDb.backupable__restore(it) }
            json.mapJsonArray("events") { EventDb.backupable__restore(it) }
            json.mapJsonArray("event_templates") { EventTemplateDb.backupable__restore(it) }
            json.mapJsonArray("notes") { NoteDb.backupable__restore(it) }
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

///

private inline fun JsonElement.mapJsonArray(
    key: String,
    block: (JsonArray) -> Unit,
) {
    this.jsonObject[key]!!.jsonArray.forEach { block(it.jsonArray) }
}

private fun List<Backupable__Item>.modelsToJsonArray() =
    JsonArray(this.map { it.backupable__backup() })
