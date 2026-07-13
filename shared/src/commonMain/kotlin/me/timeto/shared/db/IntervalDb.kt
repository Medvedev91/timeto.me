package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.IntervalSq
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getStringOrNull
import me.timeto.shared.time
import me.timeto.shared.toJsonArray
import me.timeto.shared.UiException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.absoluteValue

data class IntervalDb(
    val id: Int,
    val time: Int,
    val activityId: Int,
    val note: String?,
) : Backupable__Item {

    companion object : Backupable__Holder {

        //
        // Select

        fun anyChangeFlow(): Flow<*> =
            db.intervalQueries.anyChange().asFlow()

        suspend fun selectCount(): Int = dbIo {
            db.intervalQueries.selectCount().executeAsOne().toInt()
        }

        suspend fun selectAsc(limit: Int = Int.MAX_VALUE) = dbIo {
            selectAscSync(limit)
        }

        fun selectAscSync(limit: Int): List<IntervalDb> =
            db.intervalQueries.selectAsc(limit = limit.toLong()).asList { toDb() }

        fun selectAscFlow(limit: Int = Int.MAX_VALUE): Flow<List<IntervalDb>> =
            db.intervalQueries.selectAsc(limit = limit.toLong()).asListFlow { toDb() }

        suspend fun selectDesc(limit: Int): List<IntervalDb> = dbIo {
            db.intervalQueries.selectDesc(limit.toLong()).asList { toDb() }
        }

        fun selectDescFlow(limit: Int = Int.MAX_VALUE): Flow<List<IntervalDb>> =
            db.intervalQueries.selectDesc(limit.toLong()).asListFlow { toDb() }

        suspend fun selectBetweenTimeAsc(
            timeStart: Int,
            timeFinish: Int,
            limit: Int = Int.MAX_VALUE,
        ): List<IntervalDb> = dbIo {
            db.intervalQueries.selectBetweenTimeAsc(
                timeStart = timeStart,
                timeFinish = timeFinish,
                limit = limit.toLong(),
            ).asList { toDb() }
        }

        suspend fun selectBetweenTimeDesc(
            timeStart: Int,
            timeFinish: Int,
            limit: Int = Int.MAX_VALUE,
        ): List<IntervalDb> = dbIo {
            db.intervalQueries.selectBetweenTimeDesc(
                timeStart = timeStart,
                timeFinish = timeFinish,
                limit = limit.toLong(),
            ).asList { toDb() }
        }

        fun selectBetweenTimeDescFlow(
            timeStart: Int,
            timeFinish: Int,
            limit: Int = Int.MAX_VALUE,
        ): Flow<List<IntervalDb>> = db.intervalQueries
            .selectBetweenTimeDesc(
                timeStart = timeStart,
                timeFinish = timeFinish,
                limit = limit.toLong(),
            )
            .asListFlow { toDb() }

        fun selectFirstAndLastNeedTransaction(): Pair<IntervalDb, IntervalDb> = Pair(
            db.intervalQueries.selectAsc(limit = 1).executeAsOne().toDb(),
            db.intervalQueries.selectDesc(limit = 1).executeAsOne().toDb(),
        )

        fun selectFirstOneOrNullFlow(): Flow<IntervalDb?> = db.intervalQueries
            .selectAsc(limit = 1).asListFlow { toDb() }.map { it.firstOrNull() }

        suspend fun selectLastOneOrNull(): IntervalDb? = dbIo {
            db.intervalQueries.selectDesc(limit = 1).executeAsOneOrNull()?.toDb()
        }

        fun selectLastOneOrNullFlow(): Flow<IntervalDb?> = db.intervalQueries
            .selectDesc(limit = 1).asListFlow { toDb() }.map { it.lastOrNull() }

        fun updateActivitySync(oldActivityId: Int, newActivityId: Int) {
            db.intervalQueries.updateActivity(
                oldActivityId = oldActivityId,
                newActivityId = newActivityId,
            )
        }

        //
        // Insert

        suspend fun insertWithValidation(
            activityDb: ActivityDb,
            note: String?,
        ): IntervalDb = dbIo {
            db.transactionWithResult {
                insertWithValidation__needTransaction(
                    activityDb = activityDb,
                    note = note,
                )
            }
        }

        fun insertWithValidation__needTransaction(
            activityDb: ActivityDb,
            note: String?,
        ): IntervalDb {
            val time: Int = time()
            val activityId: Int = activityDb.id
            val note: String? = note?.let { validateNote(it) }
            db.intervalQueries.insertAutoIncremented(
                time = time,
                activityId = activityId,
                note = note,
            )
            val id: Int =
                db.intervalQueries.selectLastInsertRowId().executeAsOne().toInt()
            return IntervalDb(
                id = id,
                time = time,
                activityId = activityId,
                note = note,
            )
        }

        suspend fun insertForDemo(
            time: Int,
            activityDb: ActivityDb,
            note: String?,
        ): Unit = dbIo {
            db.intervalQueries.insertAutoIncremented(
                time = time,
                activityId = activityDb.id,
                note = note,
            )
        }

        ///

        suspend fun pauseLastInterval(): Unit = dbIo {
            db.transaction {

                val now: Int = time()
                val intervalDb: IntervalDb =
                    db.intervalQueries.selectDesc(limit = 1).executeAsOne().toDb()
                val intervalTf: TextFeatures =
                    (intervalDb.note ?: "").textFeatures()
                val intervalDbTimerType: TimerType =
                    intervalDb.buildTimerType()
                val activityDb: ActivityDb =
                    intervalDb.selectActivityDbCached()

                // region tfPaused and tfTimerType
                val tfPaused: TextFeatures.Paused
                val tfTimerType: TextFeatures.TimerType
                when (intervalDbTimerType) {
                    is TimerType.Timer -> {
                        val originalTimer: Int =
                            intervalDbTimerType.timer
                        tfPaused = intervalTf.paused ?: TextFeatures.Paused(
                            intervalId = intervalDb.id,
                            originalTimerType = TextFeatures.TimerType.Timer(originalTimer),
                        )
                        val remainingSeconds: Int =
                            intervalDbTimerType.calcRemainingSeconds(now)
                        tfTimerType =
                            if (remainingSeconds <= 0)
                                TextFeatures.TimerType.OverdueTimer(remainingSeconds.absoluteValue)
                            else TextFeatures.TimerType.Timer(remainingSeconds)
                    }
                    // todo not tested
                    is TimerType.OverdueTimer -> {
                        tfPaused = TextFeatures.Paused(
                            intervalId = intervalDb.id,
                            originalTimerType = TextFeatures.TimerType.OverdueTimer(
                                intervalDbTimerType.overdueSeconds
                            ),
                        )
                        tfTimerType = TextFeatures.TimerType.OverdueTimer(
                            intervalDbTimerType.calcOverdueSeconds(now)
                        )
                    }
                    is TimerType.Stopwatch -> {
                        val elapsedSeconds: Int =
                            intervalDbTimerType.calcElapsedSeconds(now)
                        tfPaused = TextFeatures.Paused(
                            intervalId = intervalDb.id,
                            originalTimerType = TextFeatures.TimerType.Stopwatch(startSeconds = elapsedSeconds),
                        )
                        tfTimerType = TextFeatures.TimerType.Stopwatch(
                            startSeconds = elapsedSeconds,
                        )
                    }
                }
                // endregion

                val pausedTf = (intervalDb.note ?: "").textFeatures().copy(
                    activityDb = activityDb,
                    paused = tfPaused,
                    timerType = tfTimerType,
                )

                val pausedTaskId: Int = TaskDb.insertWithValidation_transactionRequired(
                    folder = Cache.todayTaskFolderDb,
                    text = pausedTf.textWithFeatures(),
                )
                val pauseIntervalTf = "Break".textFeatures().copy(
                    pause = TextFeatures.Pause(pausedTaskId = pausedTaskId),
                    timerType = TextFeatures.TimerType.Timer(seconds = activityDb.pomodoro_timer),
                )
                insertWithValidation__needTransaction(
                    activityDb = ActivityDb.selectOtherCached(),
                    note = pauseIntervalTf.textWithFeatures(),
                )
            }
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.intervalQueries.selectAsc(Int.MAX_VALUE.toLong()).asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.intervalQueries.insertWithId(
                IntervalSq(
                    id = j.getInt(0),
                    time = j.getInt(1),
                    activity_id = j.getInt(2),
                    note = j.getStringOrNull(3),
                )
            )
        }
    }

    fun unixTime() = UnixTime(time)

    fun buildTimerType(): TimerType {
        val tfTimerType: TextFeatures.TimerType =
            note?.textFeatures()?.timerType ?: return TimerType.Stopwatch(startTime = time, startSeconds = 0)
        return when (tfTimerType) {
            is TextFeatures.TimerType.Timer ->
                TimerType.Timer(startTime = time, timer = tfTimerType.seconds)
            is TextFeatures.TimerType.OverdueTimer ->
                TimerType.OverdueTimer(startTime = time, overdueSeconds = tfTimerType.overdueSeconds)
            is TextFeatures.TimerType.Stopwatch ->
                TimerType.Stopwatch(startTime = time, startSeconds = tfTimerType.startSeconds)
        }
    }

    fun noteOrActivityName(): String {
        val noteText: String? =
            note?.textFeatures()?.textNoFeatures?.takeIf { it.isNotBlank() }
        if (noteText != null)
            return noteText
        return selectActivityDbCached().name.textFeatures().textNoFeatures
    }

    suspend fun selectActivityDb(): ActivityDb =
        ActivityDb.selectAll().first { it.id == activityId }

    fun selectActivityDbCached(): ActivityDb =
        Cache.activitiesDb.first { it.id == activityId }

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateTimer(timer: Int): Unit = dbIo {
        if (timer < 0)
            throw UiException("Invalid timer")
        val newNote: String = (note ?: "").textFeatures().copy(
            timerType = TextFeatures.TimerType.Timer(timer),
        ).textWithFeatures()
        db.intervalQueries.updateNoteById(id = id, note = newNote)
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateNote(note: String): Unit = dbIo {
        db.intervalQueries.updateNoteById(
            id = id,
            note = validateNote(note),
        )
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun updateEx(
        newTime: Int,
        newActivityDb: ActivityDb,
        newNote: String?,
    ): Unit = dbIo {
        db.transaction {
            if (newTime > time())
                throw UiException("Invalid time")
            val byTime: List<IntervalSq> =
                db.intervalQueries.selectByTime(newTime).executeAsList()
            if ((newTime != time) && byTime.isNotEmpty())
                throw UiException("Time is unavailable")
            db.intervalQueries.updateById(
                time = newTime,
                activityId = newActivityDb.id,
                note = newNote?.let { validateNote(it) },
                id = id,
            )
        }
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun moveToTasks(): Unit = dbIo {
        db.transaction {
            if (db.intervalQueries.selectCount().executeAsOne().toInt() <= 1)
                throw UiException("The only entry")
            val activityDb: ActivityDb =
                ActivityDb.selectAllSync().firstOrNull { it.id == activityId } ?: throw UiException("No activity")
            val tempText: String =
                note ?: ""
            val textTf: TextFeatures = tempText.textFeatures().copy(
                timerType = when (val timerType = buildTimerType()) {
                    is TimerType.Timer -> TextFeatures.TimerType.Timer(seconds = timerType.timer)
                    is TimerType.OverdueTimer -> TextFeatures.TimerType.OverdueTimer(overdueSeconds = timerType.overdueSeconds)
                    is TimerType.Stopwatch -> TextFeatures.TimerType.Stopwatch(startSeconds = timerType.startSeconds)
                },
                activityDb = activityDb,
            )
            TaskDb.insertWithValidation_transactionRequired(
                folder = Cache.todayTaskFolderDb,
                text = textTf.textWithFeatures(),
            )
            db.intervalQueries.deleteById(id)
        }
    }

    @Throws(UiException::class, CancellationException::class)
    suspend fun delete(): Unit = dbIo {
        db.transaction {
            if (db.intervalQueries.selectCount().executeAsOne().toInt() <= 1)
                throw UiException("The only entry")
            db.intervalQueries.deleteById(id)
        }
    }

    //
    // Backupable Item

    override fun backupable__getId(): String =
        id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, time, activityId, note,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.intervalQueries.updateById(
            id = j.getInt(0),
            time = j.getInt(1),
            activityId = j.getInt(2),
            note = j.getStringOrNull(3),
        )
    }

    override fun backupable__delete() {
        db.intervalQueries.deleteById(id)
    }

    //
    // TimerType

    sealed class TimerType {

        class Timer(
            val startTime: Int,
            val timer: Int,
        ) : TimerType() {

            val finishTime: Int =
                startTime + timer

            fun isFinished(now: Int): Boolean =
                finishTime <= now

            fun calcRemainingSeconds(now: Int): Int =
                startTime + timer - now

            fun buildExpiredString(): String {
                val totalMinutes: Int = timer / 60
                return if (totalMinutes == 1) "1 minute has expired"
                else "$totalMinutes minutes have expired"
            }
        }

        data class OverdueTimer(
            val startTime: Int,
            val overdueSeconds: Int,
        ) : TimerType() {

            fun calcOverdueSeconds(now: Int): Int =
                now - startTime + overdueSeconds
        }

        class Stopwatch(
            val startTime: Int,
            val startSeconds: Int,
        ) : TimerType() {

            fun calcElapsedSeconds(now: Int): Int =
                now - startTime + startSeconds
        }
    }
}

///

private fun IntervalSq.toDb() = IntervalDb(
    id = id,
    time = time,
    activityId = activity_id,
    note = note,
)

private fun validateNote(note: String): String? =
    note.trim().takeIf { it.isNotEmpty() }
