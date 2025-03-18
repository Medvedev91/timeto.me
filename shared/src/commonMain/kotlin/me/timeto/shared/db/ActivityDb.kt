package me.timeto.shared.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import dbsq.ActivitySQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.*
import me.timeto.shared.*
import me.timeto.shared.misc.getInt
import me.timeto.shared.misc.getString
import me.timeto.shared.misc.time
import me.timeto.shared.models.GoalFormUi
import me.timeto.shared.misc.toBoolean10
import me.timeto.shared.misc.toInt10
import me.timeto.shared.misc.toJsonArray
import me.timeto.shared.ui.UiException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max

data class ActivityDb(
    val id: Int,
    val name: String,
    val emoji: String,
    val timer: Int,
    val sort: Int,
    val type_id: Int,
    val color_rgba: String,
    val keep_screen_on: Int,
    val pomodoro_timer: Int,
    val timer_hints: String,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow(): Flow<Query<Int>> =
            db.activityQueries.anyChange().asFlow()

        suspend fun selectSorted(): List<ActivityDb> = dbIo {
            db.activityQueries.selectSorted().asList { toDb() }
        }

        fun selectSortedFlow(): Flow<List<ActivityDb>> =
            db.activityQueries.selectSorted().asListFlow { toDb() }

        suspend fun selectByIdOrNull(id: Int): ActivityDb? =
            selectSorted().firstOrNull { it.id == id }

        @Throws(UiException::class)
        fun selectOtherCached(): ActivityDb {
            val otherActivities: List<ActivityDb> =
                Cache.activitiesDbSorted.filter { it.type_id == Type.other.id }
            val size: Int = otherActivities.size
            if (size != 1)
                throw UiException("System error: selectOtherCached() size: $size")
            return otherActivities.first()
        }

        suspend fun selectByEmojiOrNull(string: String): ActivityDb? =
            selectSorted().firstOrNull { it.emoji == string }

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

        fun nextColorCached(): ColorRgba {
            val activityColors: List<String> =
                Cache.activitiesDbSorted.map { activity ->
                    activity.colorRgba.toRgbaString()
                }
            for (color in colors) {
                if (!activityColors.contains(color.toRgbaString()))
                    return color
            }
            return colors.random()
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.activityQueries.selectSorted().asList { toDb() }

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
                    emoji = j.getString(6),
                    keep_screen_on = j.getInt(7),
                    pomodoro_timer = j.getInt(8),
                    timer_hints = j.getString(9),
                )
            )
        }
    }

    val keepScreenOn: Boolean =
        keep_screen_on.toBoolean10()

    val colorRgba: ColorRgba by lazy {
        ColorRgba.fromRgbaString(color_rgba)
    }

    val timerHints: List<Int> by lazy {
        if (timer_hints.isEmpty())
            return@lazy emptyList()
        timer_hints
            .split(",")
            .map { it.toInt() }
            .sorted()
    }

    fun nameWithEmoji(isLeading: Boolean = false): String =
        if (isLeading) "$emoji $name" else "$name $emoji"

    fun getType(): Type =
        Type.entries.first { it.id == type_id }

    fun isOther(): Boolean =
        getType() == Type.other

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

    suspend fun updateSort(newSort: Int) {
        dbIo {
            db.activityQueries.updateSortById(
                id = id, sort = newSort,
            )
        }
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

    //
    // Backupable Item

    override fun backupable__getId(): String =
        id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, name, timer, sort, type_id, color_rgba,
        emoji, keep_screen_on, pomodoro_timer, timer_hints,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.activityQueries.updateById(
            id = j.getInt(0),
            name = j.getString(1),
            timer = j.getInt(2),
            sort = j.getInt(3),
            type_id = j.getInt(4),
            color_rgba = j.getString(5),
            emoji = j.getString(6),
            keep_screen_on = j.getInt(7),
            pomodoro_timer = j.getInt(8),
            timer_hints = j.getString(9),
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
}

private fun ActivitySQ.toDb() = ActivityDb(
    id = id, name = name, emoji = emoji, timer = timer, sort = sort,
    type_id = type_id, color_rgba = color_rgba, keep_screen_on = keep_screen_on,
    pomodoro_timer = pomodoro_timer, timer_hints = timer_hints,
)
