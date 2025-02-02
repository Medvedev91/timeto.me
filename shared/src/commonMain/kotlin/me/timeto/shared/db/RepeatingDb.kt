package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dbsq.RepeatingSQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*
import me.timeto.shared.misc.getInt
import me.timeto.shared.misc.getIntOrNull
import me.timeto.shared.misc.getString
import me.timeto.shared.misc.time
import me.timeto.shared.misc.toBoolean10
import me.timeto.shared.misc.toInt10
import me.timeto.shared.misc.toJsonArray
import kotlin.math.absoluteValue
import kotlin.math.max

data class RepeatingDb(
    val id: Int,
    val text: String,
    val last_day: Int,
    val type_id: Int,
    val value: String,
    val daytime: Int?,
    val is_important: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        const val LAST_DAY_OF_MONTH = 0
        const val MAX_DAY_OF_MONTH = 27

        suspend fun getAsc() = dbIo {
            db.repeatingQueries.getAsc().executeAsList().map { it.toDb() }
        }

        fun getAscFlow() = db.repeatingQueries.getAsc().asFlow()
            .mapToList(Dispatchers.IO).map { list -> list.map { it.toDb() } }

        fun todayWithOffset(): Int = UnixTime(time() - dayStartOffsetSeconds()).localDay

        suspend fun getByIdOrNull(id: Int) = dbIo {
            db.repeatingQueries.getById(id).executeAsOneOrNull()?.toDb()
        }

        suspend fun addWithValidation(
            text: String,
            period: Period,
            lastDay: Int,
            daytime: Int?,
            isImportant: Boolean,
        ) = dbIo {
            db.transaction {
                db.repeatingQueries.insert(
                    id = max(time(), db.repeatingQueries.getDesc(1).executeAsOneOrNull()?.id?.plus(1) ?: 0),
                    text = validateText(text),
                    last_day = lastDay,
                    type_id = period.type.id,
                    value_ = period.value,
                    daytime = daytime,
                    is_important = isImportant.toInt10(),
                )
            }
        }

        suspend fun syncTodaySafe(today: Int): Unit = dbIo {
            // Select within a transaction to avoid duplicate additions
            db.transaction {
                db.repeatingQueries.getAsc().executeAsList()
                    .map { it.toDb() }
                    .filter { it.getNextDay() <= today }
                    .forEach { repeating ->
                        TaskDb.addWithValidation_transactionRequired(
                            text = repeating.prepTextForTask(today),
                            folder = Cache.getTodayFolderDb(),
                        )
                        db.repeatingQueries.upLastDayById(last_day = today, id = repeating.id)
                    }
            }
        }

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.repeatingQueries.getAsc().executeAsList().map { it.toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.repeatingQueries.insert(
                id = j.getInt(0),
                text = j.getString(1),
                last_day = j.getInt(2),
                type_id = j.getInt(3),
                value_ = j.getString(4),
                daytime = j.getIntOrNull(5),
                is_important = if (j.size < 7) 0 else j.getInt(6),
            )
        }

        private fun validateText(text: String): String {
            val validatedText = text.trim()
            if (validatedText.isEmpty())
                throw UIException("Empty text")
            return validatedText
        }
    }

    fun daytimeToTimeWithDayStart(today: Int): Int? {
        val daytime = daytime ?: return null
        val dayStartOffset = dayStartOffsetSeconds()
        val dayForDaytime: Int =
            if (dayStartOffset >= 0)
                if (daytime >= dayStartOffset) today else today + 1
            else
                if (daytime >= (86_400 - dayStartOffset.absoluteValue)) today - 1 else today
        return UnixTime.byLocalDay(dayForDaytime).localDayStartTime() + daytime
    }

    fun getPeriod() = Period.build(type_id, value)

    fun getType() = TYPE.values().first { it.id == id }

    /**
     * todo check performance
     *
     * The current time does not play a role in this
     * method, it is the next date after the last adding.
     *
     * WARNING
     * Do not use DB inside, only computation, it is used inside transactions.
     */
    fun getNextDay(): Int {
        return when (val period = getPeriod()) {

            is Period.EveryNDays -> last_day + period.nDays

            is Period.DaysOfWeek -> {
                val weekDays = period.weekDays
                if (weekDays.isEmpty())
                    throw UIException("getNextDay() weekDays.isEmpty()")
                for (i in 1..7) {
                    val testDay = UnixTime.byLocalDay(last_day + i)
                    if (testDay.dayOfWeek() in period.weekDays)
                        return testDay.localDay
                }
                throw UIException("getNextDay() DaysOfWeek wtf?")
            }

            is Period.DaysOfMonth -> {
                // todo catch
                if (period.days.isEmpty())
                    throw UIException("period.days.isEmpty()")
                val lastUnixDay = UnixTime.byLocalDay(last_day)
                fun getNextMonthDay(monthDay: Int): UnixTime {
                    // The last day
                    if (monthDay == LAST_DAY_OF_MONTH) {
                        // The last day of the month of the last adding
                        val lastUnixDayEndOfMonth = lastUnixDay.lastUnixDayOfMonth()
                        // If it was added in the last day of the month (the right behavior)
                        if (lastUnixDay.localDay == lastUnixDayEndOfMonth.localDay)
                            return lastUnixDayEndOfMonth.inDays(1).lastUnixDayOfMonth()
                        return lastUnixDayEndOfMonth
                    }
                    // 30 to small in case if today is 1st and the next date also 1st.
                    // todo fix if 31, when triggering and changing the time zone backwards - crash
                    for (i in 1..32) {
                        val testDay = UnixTime.byLocalDay(last_day + i)
                        if (testDay.dayOfMonth() == monthDay)
                            return testDay
                    }
                    throw UIException("getNextDay() DaysOfMonth wtf?")
                }
                period.days.map { getNextMonthDay(it) }.minBy { it.localDay }.localDay
            }

            is Period.DaysOfYear -> {
                if (period.items.isEmpty())
                    throw UIException("Period.DaysOfYear items.isEmpty()")

                val lastUnixDay = UnixTime.byLocalDay(last_day)

                val days: List<Int> = period.items.map { item ->

                    val curYearInstant = LocalDate(lastUnixDay.year(), item.monthId, item.dayId)
                        .atStartOfDayIn(TimeZone.UTC)
                    val curYearTime = curYearInstant.epochSeconds.toInt()
                    if (lastUnixDay.utcTime() < curYearTime)
                        return@map UnixTime.byUtcTime(curYearTime).localDay

                    val nextYearInstant = LocalDate(lastUnixDay.year() + 1, item.monthId, item.dayId)
                        .atStartOfDayIn(TimeZone.UTC)
                    return@map UnixTime.byUtcTime(nextYearInstant.epochSeconds.toInt()).localDay
                }

                days.min()
            }
        }
    }

    fun prepTextForTask(day: Int): String = text
        .textFeatures()
        .copy(
            fromRepeating = TextFeatures.FromRepeating(
                id = id, day = day, time = daytimeToTimeWithDayStart(day)
            ),
            isImportant = is_important.toBoolean10(),
        )
        .textWithFeatures()

    fun getNextDayString(): String =
        UnixTime.byLocalDay(getNextDay())
            .getStringByComponents(
                UnixTime.StringComponent.dayOfWeek3,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.dayOfMonth,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.month3,
            )

    suspend fun upWithValidation(
        text: String,
        period: Period,
        daytime: Int?,
        isImportant: Boolean,
    ): Unit = dbIo {
        db.repeatingQueries.upById(
            id = id,
            text = validateText(text),
            last_day = last_day,
            type_id = period.type.id,
            value_ = period.value,
            daytime = daytime,
            is_important = isImportant.toInt10(),
        )
    }

    suspend fun delete(): Unit = dbIo {
        db.repeatingQueries.deleteById(id)
    }

    ///
    /// Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, text, last_day, type_id, value, daytime, is_important,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.repeatingQueries.upById(
            id = j.getInt(0),
            text = j.getString(1),
            last_day = j.getInt(2),
            type_id = j.getInt(3),
            value_ = j.getString(4),
            daytime = j.getIntOrNull(5),
            is_important = j.getInt(6),
        )
    }

    override fun backupable__delete() {
        db.repeatingQueries.deleteById(id)
    }

    //////

    enum class TYPE(val id: Int) {
        EVERY_N_DAYS(1),
        DAYS_OF_WEEK(2),
        DAYS_OF_MONTH(3),
        DAYS_OF_YEAR(4),
    }

    sealed interface Period {

        val type: TYPE
        val value: String
        val title: String

        companion object {

            fun build(
                typeId: Int,
                value: String,
            ) = when (TYPE.values().first { it.id == typeId }) {
                TYPE.EVERY_N_DAYS -> EveryNDays(value.toInt())
                TYPE.DAYS_OF_WEEK -> DaysOfWeek(value.split(",").map { it.toInt() })
                TYPE.DAYS_OF_MONTH -> DaysOfMonth(value.split(",").map { it.toInt() }.toSet())
                TYPE.DAYS_OF_YEAR -> DaysOfYear(
                    value // Like "1.19,4.15"
                        .split(',')
                        .map {
                            val (month, day) = it.split('.')
                            DaysOfYear.MonthDayItem(month.toInt(), day.toInt())
                        }
                )
            }
        }

        class EveryNDays(
            val nDays: Int,
        ) : Period {

            override val type = TYPE.EVERY_N_DAYS

            override val value = "$nDays"

            override val title = if (nDays == 1) "Every day" else "Every $nDays days"

            init {
                if (nDays < 1)
                    throw UIException("EveryNDays nDays < 1")
            }
        }

        class DaysOfWeek(
            val weekDays: List<Int>,
        ) : Period {

            override val type = TYPE.DAYS_OF_WEEK

            override val value: String

            override val title: String

            init {
                if (weekDays.isEmpty())
                    throw UIException("DaysOfWeek no days selected") // todo report

                if (weekDays.size != weekDays.distinct().size)
                    throw UIException("DaysOfWeek not distinct") // todo report

                if (weekDays.any { it < 0 || it > 6 })
                    throw UIException("DaysOfWeek invalid data") // todo report

                value = weekDays.joinToString(",")

                title = if (weekDays.size == 7)
                    "Every day"
                else
                    weekDays.sorted().joinToString(" ") { UnixTime.dayOfWeekNames3[it] }
            }
        }

        class DaysOfMonth(
            val days: Set<Int>,
        ) : Period {

            override val type = TYPE.DAYS_OF_MONTH

            override val value: String

            override val title: String

            init {
                if (days.isEmpty())
                    throw UIException("DaysOfMonth no days selected") // todo report

                if (days.any { it < 0 || it > MAX_DAY_OF_MONTH })
                    throw UIException("DaysOfMonth invalid data") // todo report

                value = days.joinToString(",")

                title = days
                    .sortedBy { if (it == LAST_DAY_OF_MONTH) Int.MAX_VALUE else it } // Last day в конец
                    .joinToString(", ") { day ->
                        if (day == LAST_DAY_OF_MONTH) {
                            if (days.size == 1) "Last day of month" else "Last day"
                        } else {
                            val dayStr = when (day) {
                                1 -> "1st"
                                2 -> "2nd"
                                3 -> "3rd"
                                else -> "${day}th"
                            }
                            if (days.size == 1)
                                "$dayStr of each month"
                            else
                                dayStr
                        }
                    }
            }
        }


        class DaysOfYear(
            val items: List<MonthDayItem>,
        ) : Period {

            override val type = TYPE.DAYS_OF_YEAR

            override val value = items
                .distinct()
                .sortedWith(compareBy({ it.monthId }, { it.dayId })) // todo sort on title creation
                .joinToString(",") { "${it.monthId}.${it.dayId}" } // Like "1.19,4.15"

            override val title: String

            init {
                assertOrUIException(items.isNotEmpty(), "No days selected")
                assertOrUIException(
                    items.all { it.dayId in it.getMonthData().days },
                    "DaysOfYear invalid day"
                )
                title = items.joinToString(", ") { it.getTitle(isShortOrLong = true) }
            }

            companion object {

                val months = listOf(
                    MonthData(1, 1..31),
                    MonthData(2, 1..28),
                    MonthData(3, 1..31),
                    MonthData(4, 1..30),
                    MonthData(5, 1..31),
                    MonthData(6, 1..30),
                    MonthData(7, 1..31),
                    MonthData(8, 1..31),
                    MonthData(9, 1..30),
                    MonthData(10, 1..31),
                    MonthData(11, 1..30),
                    MonthData(12, 1..31),
                )
            }

            class MonthData(
                val id: Int,
                val days: IntRange,
            ) {
                // Used on UI lists
                override fun toString() = getName()

                fun getName(): String = UnixTime.monthNames[id - 1]
            }

            // "data" to use in distinct()
            data class MonthDayItem(
                val monthId: Int,
                val dayId: Int,
            ) {

                fun getMonthData() = months.first { it.id == monthId }

                fun getTitle(
                    isShortOrLong: Boolean,
                ): String {
                    val mIdx = monthId - 1
                    return "$dayId ${if (isShortOrLong) UnixTime.monthNames3[mIdx] else UnixTime.monthNames[mIdx]}"
                }
            }
        }
    }
}

private fun RepeatingSQ.toDb() = RepeatingDb(
    id = id, text = text, last_day = last_day,
    type_id = type_id, value = value_, daytime = daytime,
    is_important = is_important,
)
