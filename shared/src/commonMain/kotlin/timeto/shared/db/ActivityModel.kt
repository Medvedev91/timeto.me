package timeto.shared.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dbsq.ActivitySQ
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.*
import timeto.shared.*
import kotlin.math.max

data class ActivityModel(
    val id: Int,
    val name: String,
    val emoji: String,
    val deadline: Int,
    val sort: Int,
    val type_id: Int,
    val color_rgba: String,
    val data_json: String,
) : Backupable__Item {

    enum class TYPE(val id: Int) {
        NORMAL(0),
        OTHER(1)
    }

    companion object : Backupable__Holder {

        fun anyChangeFlow() = db.activityQueries.anyChange().asFlow()

        ///
        /// Select many

        suspend fun getAscSorted() = dbIO {
            db.activityQueries.getAscSorted().executeAsList().map { it.toModel() }
        }

        fun getAscSortedFlow() = db.activityQueries.getAscSorted().asFlow()
            .mapToList().map { list -> list.map { it.toModel() } }

        ///
        /// Select One

        suspend fun getByIdOrNull(id: Int) = dbIO {
            db.activityQueries.getById(id).executeAsOneOrNull()?.toModel()
        }

        fun getOther(): ActivityModel {
            val activities = DI.activitiesSorted.filter { it.type_id == TYPE.OTHER.id }
            if (activities.size != 1)
                throw UIException("System error") // todo report: "getOther() size ${activities.size}"
            return activities.first()
        }

        suspend fun getByEmojiOrNull(string: String) = getAscSorted().firstOrNull { it.emoji == string }

        ///
        /// Add

        suspend fun addWithValidation(
            name: String,
            emoji: String,
            deadline: Int,
            sort: Int,
            type: TYPE,
            colorRgba: ColorRgba,
            data: ActivityModel__Data,
        ): ActivityModel = dbIO {

            if (type == TYPE.OTHER && getAscSorted().find { it.getType() == TYPE.OTHER } != null)
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
                    deadline = deadline,
                    sort = sort,
                    type_id = type.id,
                    color_rgba = colorRgba.toRgbaString(),
                    data_json = data.toJString()
                )
                db.activityQueries.insert(activitySQ)
                activitySQ.toModel()
            }
        }

        ///

        // WARNING Do not update interval's table, otherwise recursive call.
        suspend fun syncTimeHints() {
            // Logging for safety. In case of recursive looping, I'll note in the log.
            zlog("ActivityModel__.syncTimeHints()")

            val intervals = IntervalModel.getBetweenIdDesc(time() - 30 * 24 * 3600, time())
            getAscSorted().forEach { activity ->
                // Do not use "set" to save sorting by time
                val hints = mutableListOf<Int>()
                for (interval in intervals) {
                    if (interval.activity_id != activity.id)
                        continue
                    if (hints.contains(interval.deadline))
                        continue
                    hints.add(interval.deadline)
                }
                // todo check
                val oldData = activity.getData()
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
            exActivity: ActivityModel? = null,
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

        suspend fun nextColor(): ColorRgba {
            val activityColors = getAscSorted().map { activity ->
                activity.getColorRgba().toRgbaString()
            }
            for (color in colors)
                if (!activityColors.contains(color.toRgbaString()))
                    return color
            return colors.random()
        }

        private fun ActivitySQ.toModel() = ActivityModel(
            id = id, name = name, emoji = emoji, deadline = deadline, sort = sort,
            type_id = type_id, color_rgba = color_rgba, data_json = data_json
        )

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.activityQueries.getAscSorted().executeAsList().map { it.toModel() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.activityQueries.insert(
                ActivitySQ(
                    id = j.getInt(0),
                    name = j.getString(1),
                    emoji = j.getString(7),
                    deadline = j.getInt(2),
                    sort = j.getInt(3),
                    type_id = j.getInt(4),
                    color_rgba = j.getString(5),
                    data_json = j.getString(6),
                )
            )
        }
    }

    fun nameWithEmoji() = "$name $emoji"

    fun getType() = TYPE.values().first { it.id == type_id }

    fun isOther() = getType() == TYPE.OTHER

    fun getColorRgba() = ColorRgba.fromRgbaString(color_rgba)

    fun getData() = ActivityModel__Data.jParse(data_json)

    suspend fun upNameAndEmojiAndDataWithValidation(
        name: String,
        emoji: String,
        data: ActivityModel__Data,
    ) = dbIO {
        if (isOther())
            throw UIException("It's impossible to change \"Other\" activity")

        db.activityQueries.upNameAndEmojiAndData(
            id = id,
            name = validateName(name),
            emoji = validateEmoji(emoji, exActivity = this@ActivityModel),
            data_json = data.toJString()
        )
    }

    suspend fun upData(data: ActivityModel__Data) = dbIO {
        val newDataString = data.toJString()
        if (data_json != newDataString)
            db.activityQueries.upData(
                id = id, data_json = newDataString
            )
    }

    suspend fun upSort(newSort: Int) = dbIO {
        db.activityQueries.upSort(
            id = id, sort = newSort
        )
    }

    suspend fun delete() = dbIO {
        if (isOther())
            throw UIException("It's impossible to delete \"other\" activity")

        val other = getOther()
        IntervalModel
            .getAsc()
            .filter { id == it.activity_id }
            .forEach {
                it.upActivity(other)
            }

        db.activityQueries.deleteById(id)
    }

    ///
    /// Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, name, deadline, sort, type_id, color_rgba, data_json, emoji
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.activityQueries.upById(
            id = j.getInt(0),
            name = j.getString(1),
            emoji = j.getString(7),
            deadline = j.getInt(2),
            sort = j.getInt(3),
            type_id = j.getInt(4),
            color_rgba = j.getString(5),
            data_json = j.getString(6),
        )
    }

    override fun backupable__delete() {
        db.activityQueries.deleteById(id)
    }
}

data class ActivityModel__Data(
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

    suspend fun saveToActivity(activity: ActivityModel) {
        activity.upData(this)
    }

    //////

    companion object {

        fun jParse(jString: String): ActivityModel__Data {
            return try {
                val jData = Json.parseToJsonElement(jString)
                val jTimerHints = jData.jsonObject["timer_hints"]!!.jsonObject
                ActivityModel__Data(TimerHints.fromJsonObject(jTimerHints))
            } catch (e: Throwable) {
                // todo migration?
                reportApi("ActivityModel__Data.jParse() exception:\n$jString\n$e")
                buildDefault()
            }
        }

        fun buildDefault() = ActivityModel__Data(
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

        fun get(
            historyLimit: Int, // For valid UI
            customLimit: Int, // Max for valid UI
            primaryHints: List<Int> = listOf(), // To show in maximum priority
        ): List<Int> = when (type) {
            // Sorting for hints from history only after getting the last ones - take()
            HINT_TYPE.history -> {
                // We must take the most recent from the history, and sort them, so the logic is below.
                // We can only sort at the end, otherwise the old ones from the story may be at the beginning.
                val unique = (primaryHints + history_list).distinct()
                val (uPrimaryAll, uHistoryAll) = unique.partition { it in primaryHints }
                //
                if (uPrimaryAll.size >= historyLimit)
                    uPrimaryAll.sorted().take(historyLimit)
                else
                    uPrimaryAll.sorted() + uHistoryAll.take(historyLimit - uPrimaryAll.size).sorted()
            }
            HINT_TYPE.custom -> (primaryHints.sorted() + custom_list.sorted()).distinct().take(customLimit)
        }

        //////

        enum class HINT_TYPE {
            history, custom
        }
    }
}
