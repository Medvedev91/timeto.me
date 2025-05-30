package me.timeto.shared.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import dbsq.ActivitySQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.*
import me.timeto.shared.*
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.misc.getInt
import me.timeto.shared.misc.getString
import me.timeto.shared.misc.time
import me.timeto.shared.misc.toBoolean10
import me.timeto.shared.misc.toInt10
import me.timeto.shared.misc.toJsonArray
import me.timeto.shared.ui.UiException
import me.timeto.shared.ui.goals.form.GoalFormData
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

        //
        // Select

        fun anyChangeFlow(): Flow<Query<Int>> =
            db.activityQueries.anyChange().asFlow()

        private fun selectSortedSync(): List<ActivityDb> =
            db.activityQueries.selectSorted().asList { toDb() }

        suspend fun selectSorted(): List<ActivityDb> = dbIo {
            selectSortedSync()
        }

        fun selectSortedFlow(): Flow<List<ActivityDb>> =
            db.activityQueries.selectSorted().asListFlow { toDb() }

        fun selectByIdOrNullSync(id: Int): ActivityDb? =
            selectSortedSync().firstOrNull { it.id == id }

        suspend fun selectByIdOrNull(id: Int): ActivityDb? = dbIo {
            selectByIdOrNullSync(id)
        }

        fun selectByEmojiOrNullSync(string: String): ActivityDb? =
            selectSortedSync().firstOrNull { it.emoji == string }

        @Throws(UiException::class)
        fun selectOtherCached(): ActivityDb =
            Cache.activitiesDbSorted.findOther()

        ///

        suspend fun updateSortMany(
            activitiesDb: List<ActivityDb>,
        ): Unit = dbIo {
            db.transaction {
                activitiesDb.forEachIndexed { idx, activityDb ->
                    db.activityQueries.updateSortById(
                        id = activityDb.id,
                        sort = idx,
                    )
                }
            }
        }

        ///

        @Throws(UiException::class, CancellationException::class)
        suspend fun addWithValidation(
            name: String,
            emoji: String,
            timer: Int,
            sort: Int,
            type: Type,
            colorRgba: ColorRgba,
            keepScreenOn: Boolean,
            goalFormsData: List<GoalFormData>,
            pomodoroTimer: Int,
            timerHints: Set<Int>,
        ): ActivityDb = dbIo {
            db.transactionWithResult {
                val validatedName: String = validateName(name)
                val validatedEmoji: String = validateEmojiSync(emoji)
                val activitiesDb: List<ActivityDb> = selectSortedSync()
                if (type == Type.other && activitiesDb.any { it.getType() == Type.other })
                    throw UiException("System error: \"Other\" already exists")
                val lastId: Int = activitiesDb.maxOfOrNull { it.id } ?: 0
                val nextId: Int = max(time(), lastId + 1)
                val activitySQ = ActivitySQ(
                    id = nextId,
                    name = validatedName,
                    emoji = validatedEmoji,
                    timer = timer,
                    sort = sort,
                    type_id = type.id,
                    color_rgba = colorRgba.toRgbaString(),
                    keep_screen_on = keepScreenOn.toInt10(),
                    pomodoro_timer = pomodoroTimer,
                    timer_hints = timerHints.toTimerHintsDb(),
                )
                db.activityQueries.insert(activitySQ)
                val activityDb: ActivityDb = activitySQ.toDb()
                GoalDb.insertManySync(activityDb, goalFormsData)
                activityDb
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

    // todo catch exception
    val colorRgba: ColorRgba by lazy {
        ColorRgba.fromRgbaStringEx(color_rgba)
    }

    val timerHints: Set<Int> by lazy {
        if (timer_hints.isEmpty())
            return@lazy emptySet()
        timer_hints
            .split(",")
            .map { it.toInt() }
            .toSet()
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
        seconds: Int,
    ): IntervalDb = IntervalDb.insertWithValidation(
        timer = seconds,
        activityDb = this,
        note = null,
    )

    suspend fun updateTimerHints(
        newTimerHints: Set<Int>,
    ): Unit = dbIo {
        db.activityQueries.updateTimerHintsById(
            timer_hints = newTimerHints.toTimerHintsDb(),
            id = this@ActivityDb.id,
        )
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun upByIdWithValidation(
        name: String,
        emoji: String,
        keepScreenOn: Boolean,
        colorRgba: ColorRgba,
        goalFormsData: List<GoalFormData>,
        pomodoroTimer: Int,
        timerHints: Set<Int>,
    ): Unit = dbIo {
        db.transaction {
            val activityDb: ActivityDb = this@ActivityDb
            if (isOther())
                throw UiException("It's impossible to change \"Other\" activity")
            val validatedName: String = validateName(name)
            val validatedEmoji: String = validateEmojiSync(emoji, exActivity = activityDb)
            db.activityQueries.updateById(
                id = id,
                name = validatedName,
                timer = timer,
                sort = sort,
                type_id = type_id,
                color_rgba = colorRgba.toRgbaString(),
                emoji = validatedEmoji,
                keep_screen_on = keepScreenOn.toInt10(),
                pomodoro_timer = pomodoroTimer,
                timer_hints = timerHints.toTimerHintsDb(),
            )
            GoalDb.deleteByActivityDbSync(activityDb)
            GoalDb.insertManySync(activityDb, goalFormsData)
        }
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun delete(): Unit = dbIo {
        db.transaction {
            if (isOther())
                throw UiException("It's impossible to delete \"Other\" activity")
            val other: ActivityDb = selectSortedSync().findOther()
            IntervalDb
                .selectAscSync(limit = Int.MAX_VALUE)
                .filter { id == it.activity_id }
                .forEach {
                    it.updateActivitySync(newActivity = other)
                }
            GoalDb.deleteByActivityDbSync(this@ActivityDb)
            db.activityQueries.deleteById(id)
        }
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

///

@Throws(UiException::class)
private fun validateName(name: String): String {
    val validatedName: String = name.trim()
    if (validatedName.isEmpty())
        throw UiException("Empty name")
    return validatedName
}

@Throws(UiException::class)
private fun validateEmojiSync(
    emoji: String,
    exActivity: ActivityDb? = null,
): String {

    val validatedEmoji: String = emoji.trim()
    if (validatedEmoji.isEmpty())
        throw UiException("Emoji not selected")

    val activity: ActivityDb? =
        ActivityDb.selectByEmojiOrNullSync(emoji)
    if (activity == null)
        return validatedEmoji

    if (activity.id != exActivity?.id)
        throw UiException("Emoji $emoji is already used for the \"${activity.name}\" activity.")

    return validatedEmoji
}

///

@Throws(UiException::class)
private fun List<ActivityDb>.findOther(): ActivityDb {
    val otherActivities: List<ActivityDb> =
        this.filter { it.type_id == ActivityDb.Type.other.id }
    val size: Int = otherActivities.size
    if (size != 1)
        throw UiException("System error: filterOther() size: $size")
    return otherActivities.first()
}

private fun Set<Int>.toTimerHintsDb(): String =
    this.joinToString(",")

private fun ActivitySQ.toDb() = ActivityDb(
    id = id, name = name, emoji = emoji, timer = timer, sort = sort,
    type_id = type_id, color_rgba = color_rgba, keep_screen_on = keep_screen_on,
    pomodoro_timer = pomodoro_timer, timer_hints = timer_hints,
)
