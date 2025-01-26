package me.timeto.shared.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import dbsq.ActivitySQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.*
import me.timeto.shared.*
import me.timeto.shared.models.GoalFormUi
import me.timeto.shared.utils.toBoolean10
import me.timeto.shared.utils.toInt10
import kotlin.math.max

data class ActivityDb(
    val id: Int,
    val name: String,
    val emoji: String,
    val timer: Int,
    val sort: Int,
    val type_id: Int,
    val color_rgba: String,
    val data_json: String,
    val keep_screen_on: Int,
    val pomodoro_timer: Int,
) : Backupable__Item {

    enum class TYPE(val id: Int) {
        NORMAL(0),
        OTHER(1)
    }

    companion object : Backupable__Holder {

        fun anyChangeFlow(): Flow<Query<Int>> =
            db.activityQueries.anyChange().asFlow()

        ///

        suspend fun selectAllSorted(): List<ActivityDb> = dbIo {
            db.activityQueries.selectAllSorted().executeAsList().map { it.toDb() }
        }

        fun selectAllSortedFlow(): Flow<List<ActivityDb>> =
            db.activityQueries.selectAllSorted().asListFlow { toDb() }

        suspend fun getByIdOrNull(id: Int): ActivityDb? = dbIo {
            db.activityQueries.getById(id).executeAsOneOrNull()?.toDb()
        }

        fun getOther(): ActivityDb {
            val activities = Cache.activitiesDbSorted.filter { it.type_id == TYPE.OTHER.id }
            if (activities.size != 1)
                throw UIException("System error") // todo report: "getOther() size ${activities.size}"
            return activities.first()
        }

        suspend fun getByEmojiOrNull(string: String): ActivityDb? =
            selectAllSorted().firstOrNull { it.emoji == string }

        ///

        suspend fun addWithValidation(
            name: String,
            emoji: String,
            timer: Int,
            sort: Int,
            type: TYPE,
            colorRgba: ColorRgba,
            data: ActivityDb__Data,
            keepScreenOn: Boolean,
            goalFormsUi: List<GoalFormUi>,
            pomodoroTimer: Int,
        ): ActivityDb = dbIo {

            if (type == TYPE.OTHER && selectAllSorted().find { it.getType() == TYPE.OTHER } != null)
                throw UIException("Other already exists") // todo report

            val validatedEmoji = validateEmoji(emoji)

            db.transactionWithResult {
                val nextId = max(
                    time(),
                    db.activityQueries.getDesc(limit = 1).executeAsOneOrNull()?.id?.plus(1) ?: 0
                )
                val activitySQ = ActivitySQ(
                    id = nextId,
                    name = validateName(name),
                    emoji = validatedEmoji,
                    timer = timer,
                    sort = sort,
                    type_id = type.id,
                    color_rgba = colorRgba.toRgbaString(),
                    data_json = data.toJString(),
                    keep_screen_on = keepScreenOn.toInt10(),
                    pomodoro_timer = pomodoroTimer,
                )
                db.activityQueries.insert(activitySQ)
                val activityDb = activitySQ.toDb()
                GoalDb.insertManySync(activityDb, goalFormsUi)
                activityDb
            }
        }

        ///

        // WARNING Do not update interval's table, otherwise recursive call.
        suspend fun syncTimeHints() {
            // Logging for safety. In case of recursive looping, I'll note in the log.
            zlog("ActivityModel__.syncTimeHints()")

            val intervals = IntervalDb.getBetweenIdDesc(time() - 30 * 24 * 3600, time())
            selectAllSorted().forEach { activity ->
                // Do not use "set" to save sorting by time
                val hints = mutableListOf<Int>()
                for (interval in intervals) {
                    if (interval.activity_id != activity.id)
                        continue
                    if (hints.contains(interval.timer))
                        continue
                    if (interval.note?.textFeatures()?.paused != null)
                        continue
                    hints.add(interval.timer)
                }
                // todo check
                val oldData = activity.data
                val newData = oldData.copy(
                    timer_hints = oldData.timer_hints.copy(
                        history_list = hints
                    )
                )
                newData.saveToActivity(activity)
            }
        }

        fun validateName(name: String): String {
            val validatedName = name.trim()
            if (validatedName.isEmpty())
                throw UIException("Empty name")
            return validatedName
        }

        suspend fun validateEmoji(
            emoji: String,
            exActivity: ActivityDb? = null,
        ): String {
            val validatedEmoji = emoji.trim()
            if (validatedEmoji.isEmpty())
                throw UIException("Emoji not selected")

            val activity = getByEmojiOrNull(emoji) ?: return validatedEmoji

            if (activity.id != exActivity?.id)
                throw UIException("Emoji $emoji is already used for the \"${activity.name}\" activity.")

            return validatedEmoji
        }


        /// attractiveness. In fillInitData() hardcode by indexes.
        /// https://developer.apple.com/design/human-interface-guidelines/ios/visual-design/color
        /// https://material.io/resources/color
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

        fun nextColorDI(): ColorRgba {
            val activityColors = Cache.activitiesDbSorted.map { activity ->
                activity.colorRgba.toRgbaString()
            }
            for (color in colors)
                if (!activityColors.contains(color.toRgbaString()))
                    return color
            return colors.random()
        }

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.activityQueries.selectAllSorted().executeAsList().map { it.toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.activityQueries.insert(
                ActivitySQ(
                    id = j.getInt(0),
                    name = j.getString(1),
                    timer = j.getInt(2),
                    sort = j.getInt(3),
                    type_id = j.getInt(4),
                    color_rgba = j.getString(5),
                    data_json = j.getString(6),
                    emoji = j.getString(7),
                    keep_screen_on = j.getInt(8),
                    pomodoro_timer = j.getInt(9),
                )
            )
        }
    }

    val keepScreenOn = keep_screen_on.toBoolean10()

    val colorRgba: ColorRgba by lazy {
        ColorRgba.fromRgbaString(color_rgba)
    }

    val data: ActivityDb__Data by lazy {
        ActivityDb__Data.jParse(data_json)
    }

    fun nameWithEmoji(isLeading: Boolean = false) =
        if (isLeading) "$emoji $name" else "$name $emoji"

    fun getType() = TYPE.values().first { it.id == type_id }

    fun isOther() = getType() == TYPE.OTHER

    fun getGoalsDbCached(): List<GoalDb> =
        Cache.goalsDb.filter { it.activity_id == id }

    suspend fun startInterval(
        timer: Int,
    ): IntervalDb = IntervalDb.addWithValidation(
        timer = timer,
        activity = this,
        note = null,
    )

    // todo use transaction
    suspend fun upByIdWithValidation(
        name: String,
        emoji: String,
        data: ActivityDb__Data,
        keepScreenOn: Boolean,
        colorRgba: ColorRgba,
        goalFormsUi: List<GoalFormUi>,
        pomodoroTimer: Int,
    ) = dbIo {
        if (isOther())
            throw UIException("It's impossible to change \"Other\" activity")

        db.activityQueries.upById(
            id = id,
            name = validateName(name),
            timer = timer,
            sort = sort,
            type_id = type_id,
            color_rgba = colorRgba.toRgbaString(),
            data_json = data.toJString(),
            emoji = validateEmoji(emoji, exActivity = this@ActivityDb),
            keep_screen_on = keepScreenOn.toInt10(),
            pomodoro_timer = pomodoroTimer,
        )

        GoalDb.deleteByActivityDbSync(this@ActivityDb)
        GoalDb.insertManySync(this@ActivityDb, goalFormsUi)
    }

    suspend fun upData(data: ActivityDb__Data) = dbIo {
        val newDataString = data.toJString()
        if (data_json != newDataString)
            db.activityQueries.upData(
                id = id, data_json = newDataString
            )
    }

    suspend fun upSort(newSort: Int) = dbIo {
        db.activityQueries.upSort(
            id = id, sort = newSort
        )
    }

    // todo use transaction
    suspend fun delete() = dbIo {
        if (isOther())
            throw UIException("It's impossible to delete \"other\" activity")

        val other = getOther()
        IntervalDb
            .getAsc()
            .filter { id == it.activity_id }
            .forEach {
                it.upActivity(other)
            }

        GoalDb.deleteByActivityDbSync(this@ActivityDb)
        db.activityQueries.deleteById(id)
    }

    ///
    /// Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, name, timer, sort, type_id, color_rgba,
        data_json, emoji, keep_screen_on,
        pomodoro_timer,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.activityQueries.upById(
            id = j.getInt(0),
            name = j.getString(1),
            timer = j.getInt(2),
            sort = j.getInt(3),
            type_id = j.getInt(4),
            color_rgba = j.getString(5),
            data_json = j.getString(6),
            emoji = j.getString(7),
            keep_screen_on = j.getInt(8),
            pomodoro_timer = j.getInt(9),
        )
    }

    override fun backupable__delete() {
        db.activityQueries.deleteById(id)
    }
}

private fun ActivitySQ.toDb() = ActivityDb(
    id = id, name = name, emoji = emoji, timer = timer, sort = sort,
    type_id = type_id, color_rgba = color_rgba, data_json = data_json,
    keep_screen_on = keep_screen_on, pomodoro_timer = pomodoro_timer,
)

data class ActivityDb__Data(
    val timer_hints: TimerHints,
) {

    fun toJString(): String {
        val map = mapOf(
            "timer_hints" to timer_hints.toJsonObject()
        )
        return JsonObject(map).toString()
    }

    fun assertValidity() {
        when (timer_hints.type) {
            TimerHints.HINT_TYPE.custom -> {
                if (timer_hints.custom_list.isEmpty())
                    throw UIException("Empty custom timer hints")
            }
            TimerHints.HINT_TYPE.history -> {}
        }
    }

    suspend fun saveToActivity(activity: ActivityDb) {
        activity.upData(this)
    }

    //////

    companion object {

        fun jParse(jString: String): ActivityDb__Data = try {
            val jData = Json.parseToJsonElement(jString)
            val jTimerHints = jData.jsonObject["timer_hints"]!!.jsonObject
            ActivityDb__Data(TimerHints.fromJsonObject(jTimerHints))
        } catch (e: Throwable) {
            // todo migration?
            reportApi("ActivityModel__Data.jParse() exception:\n$jString\n$e")
            buildDefault()
        }

        fun buildDefault() = ActivityDb__Data(
            timer_hints = TimerHints(
                type = TimerHints.HINT_TYPE.history,
                default_list = listOf(),
                custom_list = listOf(),
                history_list = listOf()
            )
        )
    }

    //////

    data class TimerHints(
        val type: HINT_TYPE,
        val default_list: List<Int>, // Seconds
        val custom_list: List<Int>, // Seconds
        val history_list: List<Int>, // Seconds
    ) {

        companion object {

            fun fromJsonObject(j: JsonObject) = TimerHints(
                type = HINT_TYPE.valueOf(j.getString("type")),
                default_list = j.getIntArray("default_list"),
                custom_list = j.getIntArray("custom_list"),
                history_list = j.getIntArray("history_list"),
            )
        }

        fun toJsonObject() = JsonObject(
            mapOf(
                "type" to JsonPrimitive(type.name),
                "default_list" to default_list.toJsonArray(),
                "custom_list" to custom_list.toJsonArray(),
                "history_list" to history_list.toJsonArray(),
            )
        )

        fun getTimerHintsUI(
            historyLimit: Int,
            customLimit: Int,
            onSelect: suspend (TimerHintUI) -> Unit,
        ): List<TimerHintUI> {

            val secondsList = when (type) {
                HINT_TYPE.history -> history_list.sorted().distinct().take(historyLimit)
                HINT_TYPE.custom -> custom_list.sorted().distinct().take(customLimit)
            }

            return secondsList.map { seconds ->
                TimerHintUI(
                    seconds = seconds,
                    onSelect = onSelect,
                )
            }
        }

        class TimerHintUI(
            val seconds: Int,
            val onSelect: suspend (TimerHintUI) -> Unit,
        ) {

            val text = seconds.toTimerHintNote(isShort = true)

            fun startInterval(
                onSuccess: (() -> Unit) = {},
            ) {
                launchExDefault {
                    onSelect(this@TimerHintUI)
                    onSuccess()
                }
            }
        }

        //////

        enum class HINT_TYPE {
            history, custom
        }
    }
}
