package me.timeto.shared.db

import dbsq.ActivitySQ
import dbsq.Goal2Sq
import dbsq.GoalSq
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.UiException
import me.timeto.shared.UnixTime
import me.timeto.shared.db.Goal2Db.Period
import me.timeto.shared.removeDuplicateSpaces
import me.timeto.shared.time
import me.timeto.shared.zlog

// todo can't delete "Other" type
data class Goal2Db(
    val id: Int,
    val parent_id: Int?,
    val type_id: Int,
    val name: String,
    val seconds: Int,
    val timer: Int,
    val period_json: String,
    val finish_text: String,
    val home_button_sort: String,
    val color_rgba: String,
    val keep_screen_on: Int,
    val pomodoro_timer: Int,
) {

    companion object {

        //
        // Select

        suspend fun selectAll(): List<Goal2Db> =
            dbIo { selectAllSync() }

        private fun selectAllSync(): List<Goal2Db> =
            db.goal2Queries.selectAll().asList { toDb() }
    }

    ///

    enum class Type(val id: Int) {
        general(0),
        other(1)
    }

    sealed interface Period {

        val type: Type

        fun isToday(): Boolean

        fun note(): String

        fun toJson(): JsonObject

        ///

        enum class Type(val id: Int) {
            daysOfWeek(1), weekly(2),
        }

        companion object {

            fun fromJson(json: JsonObject): Period {
                val typeRaw: Int = json["type"]!!.jsonPrimitive.int
                return when (typeRaw) {
                    Type.daysOfWeek.id -> DaysOfWeek.fromJson(json)
                    Type.weekly.id -> Weekly()
                    else -> throw Exception("GoalDb.Period.fromJson() type: $typeRaw")
                }
            }
        }

        ///

        class DaysOfWeek(
            val days: Set<Int>,
        ) : Period {

            companion object {

                fun fromJson(json: JsonObject) = DaysOfWeek(
                    days = json["days"]!!.jsonArray.map { it.jsonPrimitive.int }.toSet(),
                )

                @Throws(UiException::class)
                fun buildWithValidation(days: Set<Int>): DaysOfWeek {
                    if (days.isEmpty())
                        throw UiException("Days not selected")
                    if (days.any { it !in 0..6 })
                        throw UiException("Invalid days: $days")
                    return DaysOfWeek(days)
                }
            }

            ///

            override val type = Type.daysOfWeek

            override fun isToday(): Boolean =
                UnixTime().dayOfWeek() in days

            override fun note(): String {
                if (days.size == 7)
                    return "Every Day"
                // todo if size is zero?
                return days.sorted().joinToString(", ") { UnixTime.dayOfWeekNames2[it] }
            }

            override fun toJson() = JsonObject(
                mapOf(
                    "type" to JsonPrimitive(type.id),
                    "days" to JsonArray(days.map { JsonPrimitive(it) }),
                )
            )
        }

        class Weekly() : Period {

            override val type = Type.weekly

            override fun isToday(): Boolean = true

            override fun note(): String = "Weekly"

            override fun toJson() = JsonObject(
                mapOf(
                    "type" to JsonPrimitive(type.id),
                )
            )
        }
    }
}

private fun selectNextIdSync(): Int =
    db.goal2Queries.selectAll().asList { this }.maxOfOrNull { it.id }?.plus(1) ?: 1

private fun Goal2Sq.toDb() = Goal2Db(
    id = id, parent_id = parent_id, type_id = type_id, name = name,
    seconds = seconds, timer = timer, period_json = period_json,
    finish_text = finish_text, home_button_sort = home_button_sort,
    color_rgba = color_rgba, keep_screen_on = keep_screen_on,
    pomodoro_timer = pomodoro_timer,
)

//
// todo Remove migration

suspend fun activitiesMigration(): Unit = dbIo {
    db.transaction {
        if (db.kVQueries.selectByKey(KvDb.KEY.ACTIVITIES_MIGRATED.name).executeAsOneOrNull() != null)
            return@transaction

        zlog("activitiesMigration() execution")
        zlog(time())

        val allGoals1Sq = db.goalQueries.selectAll().asList { this }
        val activitiesDb = db.activityQueries.selectSorted().asList { this.toDb() }
        var lastId: Int = activitiesDb.maxOfOrNull { it.id } ?: 0
        activitiesDb.forEach { activityDb ->
            val goals1Sq = allGoals1Sq.filter { it.activity_id == activityDb.id }
            when (goals1Sq.size) {
                0 -> {
                    db.goal2Queries.insert(
                        Goal2Sq(
                            id = activityDb.id,
                            parent_id = null,
                            type_id = activityDb.type_id,
                            name = activityDb.name,
                            seconds = 3_600,
                            timer = 0,
                            period_json = Period.DaysOfWeek(setOf(0, 1, 2, 3, 4, 5, 6)).toJson().toString(),
                            finish_text = "👍",
                            home_button_sort = HomeButtonSort.parseOrDefault("").string,
                            color_rgba = activityDb.color_rgba,
                            keep_screen_on = activityDb.keep_screen_on,
                            pomodoro_timer = activityDb.pomodoro_timer,
                        )
                    )
                }

                1 -> {
                    val goal1Sq: GoalSq = goals1Sq.first()
                    val name: String = goal1Sq.note.takeIf { it.isNotBlank() } ?: activityDb.name
                    db.goal2Queries.insert(
                        Goal2Sq(
                            id = activityDb.id,
                            parent_id = null,
                            type_id = activityDb.type_id,
                            name = name,
                            seconds = goal1Sq.seconds,
                            timer = goal1Sq.timer,
                            period_json = goal1Sq.period_json,
                            finish_text = goal1Sq.finish_text,
                            home_button_sort = goal1Sq.home_button_sort,
                            color_rgba = activityDb.color_rgba,
                            keep_screen_on = activityDb.keep_screen_on,
                            pomodoro_timer = activityDb.pomodoro_timer,
                        )
                    )
                }

                else -> {
                    db.goal2Queries.insert(
                        Goal2Sq(
                            id = activityDb.id,
                            parent_id = null,
                            type_id = activityDb.type_id,
                            name = activityDb.name,
                            seconds = 3_600,
                            timer = 0,
                            period_json = Period.DaysOfWeek(setOf(0, 1, 2, 3, 4, 5, 6)).toJson().toString(),
                            finish_text = "👍",
                            home_button_sort = HomeButtonSort.parseOrDefault("").string,
                            color_rgba = activityDb.color_rgba,
                            keep_screen_on = activityDb.keep_screen_on,
                            pomodoro_timer = activityDb.pomodoro_timer,
                        )
                    )

                    ///

                    goals1Sq.forEach { goal1Sq ->
                        db.goal2Queries.insert(
                            Goal2Sq(
                                id = ++lastId,
                                parent_id = activityDb.id,
                                type_id = Goal2Db.Type.general.id,
                                name = goal1Sq.note.takeIf { it.isNotBlank() } ?: activityDb.name,
                                seconds = goal1Sq.seconds,
                                timer = goal1Sq.timer,
                                period_json = goal1Sq.period_json,
                                finish_text = goal1Sq.finish_text,
                                home_button_sort = goal1Sq.home_button_sort,
                                color_rgba = activityDb.color_rgba,
                                keep_screen_on = activityDb.keep_screen_on,
                                pomodoro_timer = activityDb.pomodoro_timer,
                            )
                        )
                    }
                }
            }
            migrationActivity(activityDb)
        }

        db.kVQueries.upsert(KvDb.KEY.ACTIVITIES_MIGRATED.name, "1")
        zlog(time())
    }
}

private val activityRegex = "#a(\\d{10})".toRegex()
private val goalRegex = "\\{\\{goal_(\\d+)\\}\\}".toRegex()

private fun migrationReplaceActivityToGoal2Id(
    string: String,
    activityId: Int,
): String {
    val matchActivity: MatchResult = activityRegex.find(string) ?: return string
    val currentActivityId: Int = matchActivity.groupValues[1].toInt()
    if (currentActivityId != activityId)
        return string
    var resText: String = string.replace(matchActivity.value, "")
    val matchGoal1: MatchResult? = goalRegex.find(resText)
    if (matchGoal1 != null)
        resText = resText.replace(matchGoal1.value, "")
    return "$resText {{goal_$activityId}}"
}

private fun migrationActivity(
    activityDb: ActivityDb,
) {
    db.repeatingQueries.selectAsc().asList { this }.forEach { repeatingSq ->
        val newText =
            migrationReplaceActivityToGoal2Id(repeatingSq.text, activityDb.id).removeDuplicateSpaces()
        db.repeatingQueries.updateTextTodoRemove(text = newText, id = repeatingSq.id)
    }
    db.eventQueries.selectAscByTime().asList { this }.forEach { eventSq ->
        val newText = migrationReplaceActivityToGoal2Id(eventSq.text, activityDb.id).removeDuplicateSpaces()
        db.eventQueries.updateTextTodoRemove(text = newText, id = eventSq.id)
    }
    db.eventTemplateQueries.selectAscSorted().asList { this }.forEach { templateSq ->
        val newText = migrationReplaceActivityToGoal2Id(templateSq.text, activityDb.id).removeDuplicateSpaces()
        db.eventTemplateQueries.updateTextTodoRemove(text = newText, id = templateSq.id)
    }
    db.taskQueries.selectAsc().asList { this }.forEach { taskSq ->
        val newText = migrationReplaceActivityToGoal2Id(taskSq.text, activityDb.id).removeDuplicateSpaces()
        db.taskQueries.updateTextTodoRemove(text = newText, id = taskSq.id)
    }

    db.intervalQueries.selectDesc(Int.MAX_VALUE.toLong()).asList { this }.forEach { intervalSq ->
        val note = intervalSq.note
        if (note != null) {
            var newNote: String = goalRegex.find(note)?.let {
                note.replace(it.value, "").removeDuplicateSpaces().trim()
            } ?: note
            newNote = activityRegex.find(newNote)?.let {
                newNote.replace(it.value, "").removeDuplicateSpaces().trim()
            } ?: newNote
            if (note != newNote) {
                db.intervalQueries.updateNoteTodoRemove(note = newNote, id = intervalSq.id)
            }
        }
    }
}

private fun ActivitySQ.toDb() = ActivityDb(
    id = id, name = name, emoji = emoji, timer = timer, sort = sort,
    type_id = type_id, color_rgba = color_rgba, keep_screen_on = keep_screen_on,
    pomodoro_timer = pomodoro_timer, timer_hints = timer_hints,
)
