package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.ActivitySq
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
import me.timeto.shared.DaytimeUi
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.TextFeatures
import me.timeto.shared.UiException
import me.timeto.shared.UnixTime
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getIntOrNull
import me.timeto.shared.getString
import me.timeto.shared.getStringOrNull
import me.timeto.shared.textFeatures
import me.timeto.shared.toBoolean10
import me.timeto.shared.toInt10
import me.timeto.shared.toJsonArray
import me.timeto.shared.vm.home.buttons.homeButtonsCellsCount
import kotlin.coroutines.cancellation.CancellationException

data class ActivityDb(
    val id: Int,
    val parent_id: Int?,
    val type_id: Int,
    val name: String,
    val goal_json: String?,
    val timer: Int,
    val period_json: String,
    val emoji: String,
    val home_button_sort: String,
    val color_rgba: String,
    val keep_screen_on: Int,
    val pomodoro_timer: Int,
    val checklist_hint: Int,
    val timer_hints: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        //
        // Select

        fun anyChangeFlow(): Flow<*> =
            db.activityQueries.anyChange().asFlow()

        suspend fun selectAll(): List<ActivityDb> = dbIo {
            db.activityQueries.selectAll().asList { toDb() }
        }

        fun selectAllSync(): List<ActivityDb> =
            db.activityQueries.selectAll().asList { toDb() }

        fun selectAllFlow(): Flow<List<ActivityDb>> =
            db.activityQueries.selectAll().asListFlow { toDb() }

        suspend fun selectByIdOrNull(id: Int): ActivityDb? =
            selectAll().firstOrNull { it.id == id }

        fun selectOtherCached(): ActivityDb =
            Cache.activitiesDb.first { it.type_id == Type.other.id }

        fun selectParentRecursiveMapCached(): Map<Int, List<ActivityDb>> {
            val all = Cache.activitiesDb
            val resMap: Map<Int, MutableList<ActivityDb>> =
                all.associate { it.id to mutableListOf() }
            all.forEach { activityDb ->
                fun addRecursive(parentActivityDb: ActivityDb) {
                    val childrenActivitiesDb =
                        all.filter { it.parent_id == parentActivityDb.id }
                    resMap[activityDb.id]!!.addAll(childrenActivitiesDb)
                    childrenActivitiesDb.forEach { addRecursive(it) }
                }
                addRecursive(activityDb)
            }
            return resMap
        }

        //
        // Insert

        @Throws(UiException::class, CancellationException::class)
        suspend fun insertWithValidation(
            name: String,
            goalType: GoalType?,
            timerType: TimerType,
            period: Period,
            emoji: String,
            colorRgba: ColorRgba,
            keepScreenOn: Boolean,
            pomodoroTimer: Int,
            timerHints: List<Int>,
            parentActivityDb: ActivityDb?,
            type: Type,
        ): ActivityDb = dbIo {
            assertIsValidName(name)
            db.transactionWithResult {
                if (type == Type.other && selectAllSync().any { it.type_id == Type.other.id })
                    throw UiException("Other already exists")
                val id = selectNextIdSync()
                val activitySq = ActivitySq(
                    id = id,
                    parent_id = parentActivityDb?.id,
                    type_id = type.id,
                    name = name,
                    goal_json = goalType?.toJson(),
                    timer = timerType.dbValue,
                    period_json = period.toJson().toString(),
                    emoji = emoji,
                    home_button_sort = HomeButtonSort.findNextPositionSync(
                        isHidden = false,
                        barSize = homeButtonsCellsCount,
                    ).string,
                    color_rgba = colorRgba.toRgbaString(),
                    keep_screen_on = keepScreenOn.toInt10(),
                    pomodoro_timer = pomodoroTimer,
                    checklist_hint = 0,
                    timer_hints = timerHints.joinToString(","),
                )
                db.activityQueries.insert(activitySq)
                activitySq.toDb()
            }
        }

        ///

        fun nextColorCached(): ColorRgba {
            val activitiesColors: List<String> =
                Cache.activitiesDb.map { activityDb ->
                    activityDb.colorRgba.toRgbaString()
                }
            for (color in colors) {
                if (!activitiesColors.contains(color.toRgbaString()))
                    return color
            }
            return colors.random()
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.activityQueries.selectAll().asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.activityQueries.insert(
                ActivitySq(
                    id = j.getInt(0),
                    parent_id = j.getIntOrNull(1),
                    type_id = j.getInt(2),
                    name = j.getString(3),
                    goal_json = j.getStringOrNull(4),
                    timer = j.getInt(5),
                    period_json = j.getString(6),
                    emoji = j.getString(7),
                    home_button_sort = j.getString(8),
                    color_rgba = j.getString(9),
                    keep_screen_on = j.getInt(10),
                    pomodoro_timer = j.getInt(11),
                    checklist_hint = j.getInt(12),
                    timer_hints = j.getString(13),
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

    fun buildTimerType(): TimerType =
        TimerType.build(dbValue = timer)

    fun buildTimerHints(): List<Int> = timer_hints
        .split(",")
        .mapNotNull { it.toIntOrNull() }
        .filter { it > 0 }
        .distinct()

    fun buildTimerHintsOrDefault(): List<Int> =
        buildTimerHints().takeIf { it.isNotEmpty() } ?: listOf(45 * 60)

    fun buildPeriod(): Period =
        Period.fromJson(Json.parseToJsonElement(period_json).jsonObject)

    fun buildGoalTypeOrNull(): GoalType? {
        if (goal_json == null)
            return null
        return GoalType.fromJson(goal_json)
    }

    suspend fun updateGoal(goalType: GoalType?): Unit = dbIo {
        db.activityQueries.updateGoalById(
            goal_json = goalType?.toJson(),
            id = id,
        )
    }

    suspend fun updateChecklistHint(value: Int): Unit = dbIo {
        db.activityQueries.updateChecklistHintById(checklist_hint = value, id = id)
    }

    suspend fun updateHomeButtonSort(
        homeButtonSort: HomeButtonSort,
    ): Unit = dbIo {
        db.activityQueries.updateHomeButtonSortById(
            home_button_sort = homeButtonSort.string,
            id = id,
        )
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateNameWithValidation(name: String): Unit = dbIo {
        assertIsValidName(name)
        db.activityQueries.updateNameById(name = name, id = id)
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateWithValidation(
        name: String,
        goalType: GoalType?,
        timerType: TimerType,
        period: Period,
        emoji: String,
        colorRgba: ColorRgba,
        keepScreenOn: Boolean,
        pomodoroTimer: Int,
        timerHints: List<Int>,
        parentActivityDb: ActivityDb?,
    ): ActivityDb = dbIo {
        assertIsValidName(name)
        db.transactionWithResult {

            if (parentActivityDb != null) {
                var nextParentActivityDb: ActivityDb = parentActivityDb
                while (true) {
                    val nextParentId = nextParentActivityDb.parent_id
                    if (nextParentId == null)
                        break
                    if (nextParentId == id)
                        throw UiException("Recursive parent activity error")
                    nextParentActivityDb = selectAllSync().first { it.id == nextParentId }
                }
            }

            db.activityQueries.updateById(
                parent_id = parentActivityDb?.id,
                type_id = type_id,
                name = name,
                goal_json = goalType?.toJson(),
                timer = timerType.dbValue,
                period_json = period.toJson().toString(),
                emoji = emoji,
                home_button_sort = home_button_sort,
                color_rgba = colorRgba.toRgbaString(),
                keep_screen_on = keepScreenOn.toInt10(),
                pomodoro_timer = pomodoroTimer,
                checklist_hint = checklist_hint,
                timer_hints = timerHints.joinToString(","),
                id = id,
            )
            selectAllSync().first { it.id == id }
        }
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun deleteWithValidation(): Unit = dbIo {
        db.transaction {
            if (type_id == Type.other.id)
                throw UiException("It's impossible to delete \"Other\" activity")
            db.activityQueries.updateParent(
                oldParentId = id,
                newParentId = parent_id,
            )
            IntervalDb.updateActivitySync(
                oldActivityId = id,
                newActivityId = parent_id ?: selectAllSync().first { it.type_id == Type.other.id }.id,
            )
            db.activityQueries.deleteById(id)
        }
    }


    //
    // Start Interval

    suspend fun startInterval(
        note: String? = null,
    ): IntervalDb {
        val activityDb = this

        TaskDb.selectAsc()
            .filter { taskDb ->
                val tf = taskDb.text.textFeatures()
                taskDb.isToday && (tf.paused != null) && (tf.activityDb?.id == activityDb.id)
            }
            .forEach { taskDb ->
                taskDb.delete()
            }

        return IntervalDb.insertWithValidation(
            activityDb = activityDb,
            note = note,
        )
    }

    suspend fun startTimer(seconds: Int): IntervalDb {
        val tfTimer = TextFeatures.TimerType.Timer(seconds)
        return startInterval("".textFeatures().copy(timerType = tfTimer).textWithFeatures())
    }

    suspend fun startStopwatch(startSeconds: Int = 0): IntervalDb {
        val tfStopwatch = TextFeatures.TimerType.Stopwatch(startSeconds = startSeconds)
        return startInterval("".textFeatures().copy(timerType = tfStopwatch).textWithFeatures())
    }

    //
    // Backupable Item

    override fun backupable__getId(): String =
        id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, parent_id, type_id, name,
        goal_json, timer, period_json, emoji,
        home_button_sort, color_rgba,
        keep_screen_on, pomodoro_timer,
        checklist_hint, timer_hints,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.activityQueries.updateById(
            id = j.getInt(0),
            parent_id = j.getIntOrNull(1),
            type_id = j.getInt(2),
            name = j.getString(3),
            goal_json = j.getStringOrNull(4),
            timer = j.getInt(5),
            period_json = j.getString(6),
            emoji = j.getString(7),
            home_button_sort = j.getString(8),
            color_rgba = j.getString(9),
            keep_screen_on = j.getInt(10),
            pomodoro_timer = j.getInt(11),
            checklist_hint = j.getInt(12),
            timer_hints = j.getString(13),
        )
    }

    override fun backupable__delete() {
        db.activityQueries.deleteById(id)
    }

    ///

    enum class Type(val id: Int) {
        general(0),
        other(1)
    }

    sealed class TimerType {

        abstract val dbValue: Int

        companion object {

            fun build(dbValue: Int): TimerType = when {
                dbValue > 0 -> FixedTimer(timer = dbValue)
                dbValue == RestOfGoal.dbValue -> RestOfGoal
                dbValue == TimerPicker.dbValue -> TimerPicker
                dbValue == StopwatchZero.dbValue -> StopwatchZero
                dbValue == StopwatchDaily.dbValue -> StopwatchDaily
                dbValue in Daytime.dbValueRange -> Daytime.build(dbValue = dbValue)
                else -> throw UiException("Unknown timer type: $dbValue")
            }
        }

        ///

        object RestOfGoal : TimerType() {
            override val dbValue = 0
        }

        object TimerPicker : TimerType() {
            override val dbValue = -1
        }

        object StopwatchZero : TimerType() {
            override val dbValue = -2
        }

        object StopwatchDaily : TimerType() {
            override val dbValue = -3
        }

        data class FixedTimer(
            val timer: Int,
        ) : TimerType() {
            override val dbValue = timer
        }

        data class Daytime(
            val dayTimeUi: DaytimeUi,
        ) : TimerType() {

            companion object {

                private const val DB_OFFSET = 100

                val dbValueRange: IntRange =
                    ((-DB_OFFSET - 3_600 * 24) + 1)..-DB_OFFSET

                fun build(dbValue: Int) = Daytime(
                    dayTimeUi = DaytimeUi.byDaytime(-(dbValue + DB_OFFSET)),
                )
            }

            override val dbValue: Int =
                -(dayTimeUi.seconds + DB_OFFSET)
        }
    }

    sealed class GoalType {

        data class Timer(
            val seconds: Int,
        ) : GoalType()

        data class Counter(
            val count: Int,
        ) : GoalType()

        object Checklist
            : GoalType()

        ///

        companion object {

            fun fromJson(jString: String): GoalType {
                val j: JsonObject = Json.parseToJsonElement(jString).jsonObject
                return when (val type = j.getString("type")) {
                    "timer" -> Timer(
                        seconds = j.getInt("seconds"),
                    )
                    "counter" -> Counter(
                        count = j.getInt("count"),
                    )
                    "checklist" -> Checklist
                    else -> throw Exception("Unknown Goal Type: $type")
                }
            }
        }

        fun toJson(): String {
            val jMap: Map<String, JsonElement> = when (val goal = this) {
                is Timer -> mapOf<String, JsonElement>(
                    "type" to JsonPrimitive("timer"),
                    "seconds" to JsonPrimitive(goal.seconds),
                )
                is Counter -> mapOf<String, JsonElement>(
                    "type" to JsonPrimitive("counter"),
                    "count" to JsonPrimitive(goal.count),
                )
                is Checklist -> mapOf<String, JsonElement>(
                    "type" to JsonPrimitive("checklist"),
                )
            }
            return JsonObject(jMap).toString()
        }
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
                    else -> throw Exception("ActivityDb.Period.fromJson() type: $typeRaw")
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

private fun ActivitySq.toDb() = ActivityDb(
    id = id, parent_id = parent_id, type_id = type_id, name = name,
    goal_json = goal_json, timer = timer, period_json = period_json,
    emoji = emoji, home_button_sort = home_button_sort,
    color_rgba = color_rgba, keep_screen_on = keep_screen_on,
    pomodoro_timer = pomodoro_timer, checklist_hint = checklist_hint,
    timer_hints = timer_hints,
)

private fun selectNextIdSync(): Int =
    db.activityQueries.selectAll().asList { this }.maxOfOrNull { it.id }?.plus(1) ?: 1

@Throws(UiException::class)
private fun assertIsValidName(name: String) {
    if (name.textFeatures().textNoFeatures.isBlank())
        throw UiException("Goal name is empty")
}

// todo apple colors
// attractiveness. In fillInitData() hardcode by indexes.
// https://developer.apple.com/design/human-interface-guidelines/ios/visual-design/color
// https://material.io/resources/color
private val colors = listOf(
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
