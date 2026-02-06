package me.timeto.shared.db

import dbsq.RepeatingSQ
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getIntOrNull
import me.timeto.shared.getString
import me.timeto.shared.time
import me.timeto.shared.toBoolean10
import me.timeto.shared.toInt10
import me.timeto.shared.toJsonArray
import me.timeto.shared.UiException
import me.timeto.shared.db.KvDb.Companion.asDayStartOffsetSeconds
import kotlin.coroutines.cancellation.CancellationException
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
    val in_calendar: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        const val LAST_DAY_OF_MONTH = 0
        const val MAX_DAY_OF_MONTH = 27

        suspend fun selectAsc(): List<RepeatingDb> = dbIo {
            db.repeatingQueries.selectAsc().asList { toDb() }
        }

        fun selectAscFlow(): Flow<List<RepeatingDb>> =
            db.repeatingQueries.selectAsc().asListFlow { toDb() }

        @Throws(UiException::class, CancellationException::class)
        suspend fun insertWithValidationEx(
            text: String,
            period: Period,
            lastDay: Int,
            daytime: Int?,
            isImportant: Boolean,
            inCalendar: Boolean,
        ) = dbIo {
            db.transaction {
                val lastId: Int? = db.repeatingQueries.selectAsc().asList { toDb() }.lastOrNull()?.id
                val nextId: Int = max(time(), lastId?.plus(1) ?: 0)
                val validatedText: String = validateTextEx(text)
                db.repeatingQueries.insert(
                    id = nextId,
                    text = validatedText,
                    last_day = lastDay,
                    type_id = period.type.id,
                    value_ = period.value,
                    daytime = daytime,
                    is_important = isImportant.toInt10(),
                    in_calendar = inCalendar.toInt10(),
                )
            }
        }

        suspend fun syncTodaySafe(today: Int): Unit = dbIo {
            // Select within a transaction to avoid duplicate additions
            db.transaction {
                val todayFolderDb: TaskFolderDb = Cache.getTodayFolderDb()
                db.repeatingQueries.selectAsc()
                    .asList { toDb() }
                    .filter { it.getNextDay() <= today }
                    .forEach { repeatingDb ->
                        TaskDb.insertWithValidation_transactionRequired(
                            text = repeatingDb.prepTextForTask(today),
                            folder = todayFolderDb,
                        )
                        db.repeatingQueries.updateLastDayById(last_day = today, id = repeatingDb.id)
                    }
            }
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.repeatingQueries.selectAsc().asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.repeatingQueries.insert(
                id = j.getInt(0),
                text = j.getString(1),
                last_day = j.getInt(2),
                type_id = j.getInt(3),
                value_ = j.getString(4),
                daytime = j.getIntOrNull(5),
                is_important = j.getInt(6),
                in_calendar = j.getInt(7),
            )
        }
    }

    val isImportant: Boolean =
        is_important.toBoolean10()

    val inCalendar: Boolean =
        in_calendar.toBoolean10()

    fun daytimeToTimeWithDayStart(today: Int): Int? {
        val daytime: Int = daytime ?: return null
        val dayStartOffset: Int =
            KvDb.KEY.DAY_START_OFFSET_SECONDS.selectOrNullCached().asDayStartOffsetSeconds()
        val dayForDaytime: Int =
            if (dayStartOffset >= 0)
                if (daytime >= dayStartOffset) today else today + 1
            else
                if (daytime >= (86_400 - dayStartOffset.absoluteValue)) today - 1 else today
        return UnixTime.byLocalDay(dayForDaytime).localDayStartTime() + daytime
    }

    fun getPeriod(): Period =
        Period.build(type_id, value)

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
                    throw UiException("getNextDay() weekDays.isEmpty(). Please contact us.")
                for (i in 1..7) {
                    val testDay = UnixTime.byLocalDay(last_day + i)
                    if (testDay.dayOfWeek() in period.weekDays)
                        return testDay.localDay
                }
                throw UiException("getNextDay() DaysOfWeek wtf?. Please contact us.")
            }

            is Period.DaysOfMonth -> {
                // todo catch
                if (period.days.isEmpty())
                    throw UiException("period.days.isEmpty(). Please contact us.")
                period.days.minOf { getNextMonthDay(last_day, it) }
            }

            is Period.DaysOfYear -> {
                if (period.items.isEmpty())
                    throw UiException("Period.DaysOfYear items.isEmpty(). Please contact us.")
                period.items.minOf { item ->
                    getNextDayOfYear(last_day, item)
                }
            }
        }
    }

    fun getNextDaysUntilDay(untilDay: Int): Set<Int> {
        val today: Int = UnixTime().localDay
        val nextDay: Int = getNextDay()
        if (untilDay < nextDay)
            return setOf()

        fun calcNDays(firstDay: Int, nDays: Int): Set<Int> {
            if (nDays == 1)
                return (firstDay..untilDay).toSet()
            val resSet: MutableSet<Int> = mutableSetOf(firstDay)
            var nextNDay: Int = firstDay
            while (true) {
                nextNDay += nDays
                if (nextNDay > untilDay)
                    break
                resSet.add(nextNDay)
            }
            return resSet
        }

        when (val period = getPeriod()) {

            is Period.EveryNDays -> {
                return calcNDays(firstDay = nextDay, period.nDays)
            }

            is Period.DaysOfWeek -> {
                val resSet: MutableSet<Int> = mutableSetOf()
                for (i in 1..7) {
                    val testUnixDay = UnixTime.byLocalDay(today + i)
                    if (testUnixDay.dayOfWeek() in period.weekDays)
                        resSet.addAll(calcNDays(firstDay = testUnixDay.localDay, nDays = 7))
                }
                return resSet
            }

            is Period.DaysOfMonth -> {
                val resSet: MutableSet<Int> = mutableSetOf()
                period.days.forEach { dayOfMonth ->
                    var nextMonthDay: Int = today
                    while (true) {
                        nextMonthDay = getNextMonthDay(nextMonthDay, dayOfMonth)
                        if (nextMonthDay > untilDay)
                            break
                        resSet.add(nextMonthDay)
                    }
                }
                return resSet
            }

            is Period.DaysOfYear -> {
                val resSet: MutableSet<Int> = mutableSetOf()
                period.items.forEach { monthDayItem ->
                    var nextYearDay: Int = today
                    while (true) {
                        nextYearDay = getNextDayOfYear(nextYearDay, monthDayItem)
                        if (nextYearDay > untilDay)
                            break
                        resSet.add(nextYearDay)
                    }
                }
                return resSet
            }
        }
    }

    fun isInDay(unixDay: Int): Boolean {
        return when (val period = getPeriod()) {
            is Period.EveryNDays -> {
                ((unixDay - last_day) % period.nDays) == 0
            }

            is Period.DaysOfWeek -> {
                UnixTime.byLocalDay(unixDay).dayOfWeek() in period.weekDays
            }

            is Period.DaysOfMonth -> {
                val unixTime = UnixTime.byLocalDay(unixDay)
                period.days.any { dayOfMonth ->
                    if (dayOfMonth == LAST_DAY_OF_MONTH)
                        return@any unixTime.lastUnixDayOfMonth().localDay == unixDay
                    dayOfMonth == unixTime.dayOfMonth()
                }
            }

            is Period.DaysOfYear -> {
                val unixTime = UnixTime.byLocalDay(unixDay)
                val month: Int = unixTime.month()
                val day: Int = unixTime.dayOfMonth()
                period.items.any { monthDay ->
                    monthDay.monthId == month && monthDay.dayId == day
                }
            }
        }
    }

    fun prepTextForTask(day: Int): String = text
        .textFeatures()
        .copy(
            fromRepeating = TextFeatures.FromRepeating(
                id = id,
                day = day,
                time = daytimeToTimeWithDayStart(day),
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

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateWithValidationEx(
        text: String,
        period: Period,
        daytime: Int?,
        isImportant: Boolean,
        inCalendar: Boolean,
    ): Unit = dbIo {
        db.repeatingQueries.updateById(
            id = id,
            text = validateTextEx(text),
            last_day = last_day,
            type_id = period.type.id,
            value_ = period.value,
            daytime = daytime,
            is_important = isImportant.toInt10(),
            in_calendar = inCalendar.toInt10(),
        )
    }

    suspend fun delete(): Unit = dbIo {
        db.repeatingQueries.deleteById(id)
    }

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, text, last_day, type_id, value, daytime, is_important, in_calendar,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.repeatingQueries.updateById(
            id = j.getInt(0),
            text = j.getString(1),
            last_day = j.getInt(2),
            type_id = j.getInt(3),
            value_ = j.getString(4),
            daytime = j.getIntOrNull(5),
            is_important = j.getInt(6),
            in_calendar = j.getInt(7),
        )
    }

    override fun backupable__delete() {
        db.repeatingQueries.deleteById(id)
    }

    ///

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
            ) = when (TYPE.entries.first { it.id == typeId }) {
                TYPE.EVERY_N_DAYS -> EveryNDays(value.toInt())
                TYPE.DAYS_OF_WEEK -> DaysOfWeek(value.split(",").map { it.toInt() }.toSet())
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

            override val title: String =
                if (nDays == 1) "Every day" else "Every $nDays days"

            init {
                if (nDays < 1)
                    throw UiException("EveryNDays nDays < 1")
            }
        }

        class DaysOfWeek(
            val weekDays: Set<Int>,
        ) : Period {

            override val type = TYPE.DAYS_OF_WEEK

            override val value: String

            override val title: String

            init {
                if (weekDays.isEmpty())
                    throw UiException("DaysOfWeek no days selected")

                if (weekDays.size != weekDays.distinct().size)
                    throw UiException("DaysOfWeek not distinct")

                if (weekDays.any { it !in 0..6 })
                    throw UiException("DaysOfWeek invalid data")

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
                    throw UiException("DaysOfMonth no days selected.")

                if (days.any { it !in 0..MAX_DAY_OF_MONTH })
                    throw UiException("DaysOfMonth invalid data.")

                value = days.joinToString(",")

                title = days
                    .sortedBy { if (it == LAST_DAY_OF_MONTH) Int.MAX_VALUE else it } // Last day at the end
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

            override val value: String = items
                .distinct()
                .sortedWith(compareBy({ it.monthId }, { it.dayId })) // todo sort on title creation
                .joinToString(",") { "${it.monthId}.${it.dayId}" } // Like "1.19,4.15"

            override val title: String

            init {
                if (items.isEmpty())
                    throw UiException("No days selected")
                if (items.any { it.dayId !in it.monthData.days })
                    throw UiException("DaysOfYear invalid day")
                title = items.joinToString(", ") { it.shortTitle }
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
                val name: String =
                    UnixTime.monthNames[id - 1]
            }

            // "data" to use in distinct()
            data class MonthDayItem(
                val monthId: Int,
                val dayId: Int,
            ) {

                val monthData: MonthData =
                    months.first { it.id == monthId }

                val shortTitle: String =
                    "$dayId ${UnixTime.monthNames3[monthId - 1]}"

                val longTitle: String =
                    "$dayId ${UnixTime.monthNames[monthId - 1]}"
            }
        }
    }
}

private fun RepeatingSQ.toDb() = RepeatingDb(
    id = id, text = text, last_day = last_day,
    type_id = type_id, value = value_, daytime = daytime,
    is_important = is_important, in_calendar = in_calendar,
)

@Throws(UiException::class)
private fun validateTextEx(text: String): String {
    val validatedText: String = text.trim()
    if (validatedText.isEmpty())
        throw UiException("Empty text")
    return validatedText
}

private fun getNextMonthDay(fromDay: Int, monthDay: Int): Int {
    val fromUnixDay = UnixTime.byLocalDay(fromDay)
    if (monthDay == RepeatingDb.LAST_DAY_OF_MONTH) {
        // The last day of the month of the last adding
        val lastUnixDayEndOfMonth = fromUnixDay.lastUnixDayOfMonth()
        // If it was added in the last day of the month (the right behavior)
        if (fromUnixDay.localDay == lastUnixDayEndOfMonth.localDay)
            return lastUnixDayEndOfMonth.inDays(1).lastUnixDayOfMonth().localDay
        return lastUnixDayEndOfMonth.localDay
    }
    val curMonthDate = LocalDate(fromUnixDay.year(), fromUnixDay.month(), monthDay)
    val curMonthDay: Int = curMonthDate.toEpochDays()
    if (curMonthDay > fromDay)
        return curMonthDay
    return curMonthDate.plus(1, DateTimeUnit.MONTH).toEpochDays()
}

private fun getNextDayOfYear(
    fromDay: Int,
    monthDay: RepeatingDb.Period.DaysOfYear.MonthDayItem,
): Int {
    val fromUnixDay = UnixTime.byLocalDay(fromDay)
    val curYearDate = LocalDate(fromUnixDay.year(), monthDay.monthId, monthDay.dayId)
    val curYearDay = curYearDate.toEpochDays()
    if (curYearDay > fromDay)
        return curYearDay
    return curYearDate.plus(1, DateTimeUnit.YEAR).toEpochDays()
}
