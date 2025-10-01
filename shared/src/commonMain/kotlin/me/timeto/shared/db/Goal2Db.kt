package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.ActivitySQ
import dbsq.Goal2Sq
import dbsq.GoalSq
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.DayBarsUi
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.UiException
import me.timeto.shared.UnixTime
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.db.Goal2Db.Period
import me.timeto.shared.getInt
import me.timeto.shared.getString
import me.timeto.shared.removeDuplicateSpaces
import me.timeto.shared.textFeatures
import me.timeto.shared.time
import me.timeto.shared.toBoolean10
import me.timeto.shared.toInt10
import me.timeto.shared.toJsonArray
import me.timeto.shared.zlog
import kotlin.coroutines.cancellation.CancellationException

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
) : Backupable__Item {

    companion object : Backupable__Holder {

        //
        // Select

        fun anyChangeFlow(): Flow<*> =
            db.goal2Queries.anyChange().asFlow()

        suspend fun selectAll(): List<Goal2Db> =
            dbIo { selectAllSync() }

        fun selectAllFlow(): Flow<List<Goal2Db>> =
            db.goal2Queries.selectAll().asListFlow { toDb() }

        fun selectAllSync(): List<Goal2Db> =
            db.goal2Queries.selectAll().asList { toDb() }

        fun selectOtherCached(): Goal2Db =
            Cache.goals2Db.first { it.type_id == Type.other.id }

        fun selectParentRecursiveMapCached(): Map<Int, List<Goal2Db>> {
            val all = Cache.goals2Db
            val resMap: Map<Int, MutableList<Goal2Db>> =
                all.associate { it.id to mutableListOf() }
            all.forEach { goalDb ->
                fun addRecursive(parentGoalDb: Goal2Db) {
                    val childrenGoalsDb = all.filter { it.parent_id == parentGoalDb.id }
                    resMap[goalDb.id]!!.addAll(childrenGoalsDb)
                    childrenGoalsDb.forEach { addRecursive(it) }
                }
                addRecursive(goalDb)
            }
            return resMap
        }

        //
        // Insert

        @Throws(UiException::class, CancellationException::class)
        suspend fun insertWithValidation(
            name: String,
            seconds: Int,
            timer: Int,
            period: Period,
            colorRgba: ColorRgba,
            keepScreenOn: Boolean,
            pomodoroTimer: Int,
            parentGoalDb: Goal2Db?,
            type: Type,
        ): Goal2Db = dbIo {
            assertIsValidName(name)
            db.transactionWithResult {
                if (type == Type.other && selectAllSync().any { it.type_id == Type.other.id })
                    throw UiException("Other already exists")
                val id = selectNextIdSync()
                val goal2Sq = Goal2Sq(
                    id = id,
                    parent_id = parentGoalDb?.id,
                    type_id = type.id,
                    name = name,
                    seconds = seconds,
                    timer = timer,
                    period_json = period.toJson().toString(),
                    finish_text = "👍",
                    home_button_sort = HomeButtonSort.parseOrDefault("").string,
                    color_rgba = colorRgba.toRgbaString(),
                    keep_screen_on = keepScreenOn.toInt10(),
                    pomodoro_timer = pomodoroTimer,
                )
                db.goal2Queries.insert(goal2Sq)
                goal2Sq.toDb()
            }
        }

        ///

        // todo apple colors
        // attractiveness. In fillInitData() hardcode by indexes.
        // https://developer.apple.com/design/human-interface-guidelines/ios/visual-design/color
        // https://material.io/resources/color
        val colors = listOf(
            ColorRgba(52, 199, 89), // Green
            ColorRgba(0, 122, 255), // Blue
            ColorRgba(255, 59, 48), // Red
            ColorRgba(255, 204, 0), // Yellow
            ColorRgba(175, 82, 222), // Purple
            ColorRgba(255, 149, 0), // Orange
            ColorRgba(48, 176, 199), // Teal
            ColorRgba(88, 86, 214), // Indigo
            ColorRgba(96, 125, 139), // MD blue gray 500
            ColorRgba(162, 132, 94), // UIColor.systemBrown
            ColorRgba(142, 142, 147), // UIColor.systemGray
            ColorRgba(255, 112, 67), // MD deep orange 400
            ColorRgba(198, 255, 0), // MD lime A_400
        )

        fun nextColorCached(): ColorRgba {
            val goalsColors: List<String> =
                Cache.goals2Db.map { goalDb ->
                    goalDb.colorRgba.toRgbaString()
                }
            for (color in colors) {
                if (!goalsColors.contains(color.toRgbaString()))
                    return color
            }
            return colors.random()
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.goal2Queries.selectAll().asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.goal2Queries.insert(
                Goal2Sq(
                    id = j.getInt(0),
                    parent_id = j.getInt(1),
                    type_id = j.getInt(2),
                    name = j.getString(3),
                    seconds = j.getInt(4),
                    timer = j.getInt(5),
                    period_json = j.getString(6),
                    finish_text = j.getString(7),
                    home_button_sort = j.getString(8),
                    color_rgba = j.getString(9),
                    keep_screen_on = j.getInt(10),
                    pomodoro_timer = j.getInt(11),
                )
            )
        }
    }

    val isOther: Boolean =
        type_id == Type.other.id

    // todo catch exception
    val colorRgba: ColorRgba by lazy {
        ColorRgba.fromRgbaStringEx(color_rgba)
    }

    val keepScreenOn: Boolean =
        keep_screen_on.toBoolean10()

    fun buildPeriod(): Period =
        Period.fromJson(Json.parseToJsonElement(period_json).jsonObject)

    suspend fun updateHomeButtonSort(
        homeButtonSort: HomeButtonSort,
    ): Unit = dbIo {
        db.goal2Queries.updateHomeButtonSortById(
            home_button_sort = homeButtonSort.string,
            id = id,
        )
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateWithValidation(
        name: String,
        seconds: Int,
        timer: Int,
        period: Period,
        colorRgba: ColorRgba,
        keepScreenOn: Boolean,
        pomodoroTimer: Int,
        parentGoalDb: Goal2Db?,
    ): Goal2Db = dbIo {
        assertIsValidName(name)
        db.transactionWithResult {

            if (parentGoalDb != null) {
                var nextParentGoalDb: Goal2Db = parentGoalDb
                while (true) {
                    val nextParentId = nextParentGoalDb.parent_id
                    if (nextParentId == null)
                        break
                    if (nextParentId == id)
                        throw UiException("Recursive parent goal error")
                    nextParentGoalDb = selectAllSync().first { it.id == nextParentId }
                }
            }

            db.goal2Queries.updateById(
                parent_id = parentGoalDb?.id,
                type_id = type_id,
                name = name,
                seconds = seconds,
                timer = timer,
                period_json = period.toJson().toString(),
                finish_text = finish_text,
                home_button_sort = home_button_sort,
                color_rgba = colorRgba.toRgbaString(),
                keep_screen_on = keepScreenOn.toInt10(),
                pomodoro_timer = pomodoroTimer,
                id = id,
            )
            selectAllSync().first { it.id == id }
        }
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun deleteWithValidation(): Unit = dbIo {
        db.transaction {
            if (type_id == Type.other.id)
                throw UiException("It's impossible to delete \"Other\" goal")
            val otherGoalDb: Goal2Db =
                selectAllSync().first { it.type_id == Type.other.id }
            IntervalDb
                .selectAscSync(limit = Int.MAX_VALUE)
                .filter { id == it.activity_id }
                .forEach {
                    it.updateGoalSync(newGoalDb = otherGoalDb)
                }
            db.goal2Queries.deleteById(id)
        }
    }

    suspend fun startInterval(
        barsGoalStats: DayBarsUi.GoalStats,
    ): IntervalDb {
        val goalDb = this

        val timer: Int =
            barsGoalStats.calcTimer()

        TaskDb.selectAsc()
            .filter { taskDb ->
                val tf = taskDb.text.textFeatures()
                taskDb.isToday && (tf.paused != null) && (tf.goalDb?.id == goalDb.id)
            }
            .forEach { taskDb ->
                taskDb.delete()
            }

        return IntervalDb.insertWithValidation(
            timer = timer,
            goalDb = goalDb,
            note = null,
        )
    }

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, parent_id, type_id, name,
        seconds, timer, period_json, finish_text,
        home_button_sort, color_rgba,
        keep_screen_on, pomodoro_timer,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.goal2Queries.updateById(
            id = j.getInt(0),
            parent_id = j.getInt(1),
            type_id = j.getInt(2),
            name = j.getString(3),
            seconds = j.getInt(4),
            timer = j.getInt(5),
            period_json = j.getString(6),
            finish_text = j.getString(7),
            home_button_sort = j.getString(8),
            color_rgba = j.getString(9),
            keep_screen_on = j.getInt(10),
            pomodoro_timer = j.getInt(11),
        )
    }

    override fun backupable__delete() {
        db.goal2Queries.deleteById(id)
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

                val everyDay = DaysOfWeek(setOf(0, 1, 2, 3, 4, 5, 6))

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

@Throws(UiException::class)
private fun assertIsValidName(name: String) {
    if (name.textFeatures().textNoFeatures.isBlank())
        throw UiException("Goal name is empty")
}

//
// todo Remove migration

suspend fun activitiesMigration(): Unit = dbIo {
    db.transaction {
        if (db.kVQueries.selectByKey(KvDb.KEY.ACTIVITIES_MIGRATED.name).executeAsOneOrNull() != null)
            return@transaction

        zlog("activitiesMigration() execution")
        zlog(time())

        val allGoals1Sq = db.goalQueries.selectAll().asList { this }
        val activitiesSq = db.activityQueries.selectSorted().asList { this }
        var lastId: Int = activitiesSq.maxOfOrNull { it.id } ?: 0
        activitiesSq.forEach { activityDb ->
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
                            period_json = Period.DaysOfWeek.everyDay.toJson().toString(),
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
                            period_json = Period.DaysOfWeek.everyDay.toJson().toString(),
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
    activitySq: ActivitySQ,
) {
    db.repeatingQueries.selectAsc().asList { this }.forEach { repeatingSq ->
        val newText =
            migrationReplaceActivityToGoal2Id(repeatingSq.text, activitySq.id).removeDuplicateSpaces()
        db.repeatingQueries.updateTextTodoRemove(text = newText, id = repeatingSq.id)
    }
    db.eventQueries.selectAscByTime().asList { this }.forEach { eventSq ->
        val newText = migrationReplaceActivityToGoal2Id(eventSq.text, activitySq.id).removeDuplicateSpaces()
        db.eventQueries.updateTextTodoRemove(text = newText, id = eventSq.id)
    }
    db.eventTemplateQueries.selectAscSorted().asList { this }.forEach { templateSq ->
        val newText = migrationReplaceActivityToGoal2Id(templateSq.text, activitySq.id).removeDuplicateSpaces()
        db.eventTemplateQueries.updateTextTodoRemove(text = newText, id = templateSq.id)
    }
    db.taskQueries.selectAsc().asList { this }.forEach { taskSq ->
        val newText = migrationReplaceActivityToGoal2Id(taskSq.text, activitySq.id).removeDuplicateSpaces()
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
