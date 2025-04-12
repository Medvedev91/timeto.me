package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.IntervalSQ
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*
import me.timeto.shared.misc.getInt
import me.timeto.shared.misc.getStringOrNull
import me.timeto.shared.misc.time
import me.timeto.shared.misc.toJsonArray

data class IntervalDb(
    val id: Int,
    val timer: Int,
    val note: String?,
    val activity_id: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        const val HOT_INTERVALS_LIMIT = 200 // todo 200? Remember limit for WatchToIosSync

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

        suspend fun selectBetweenIdDesc(
            timeStart: Int,
            timeFinish: Int,
            limit: Int = Int.MAX_VALUE,
        ): List<IntervalDb> = dbIo {
            db.intervalQueries.selectBetweenIdDesc(
                timeStart = timeStart,
                timeFinish = timeFinish,
                limit = limit.toLong(),
            ).asList { toDb() }
        }

        fun selectFirstAndLastNeedTransaction(): Pair<IntervalDb, IntervalDb> = Pair(
            db.intervalQueries.selectAsc(limit = 1).executeAsOne().toDb(),
            db.intervalQueries.selectDesc(limit = 1).executeAsOne().toDb(),
        )

        suspend fun selectByIdOrNull(id: Int): IntervalDb? = dbIo {
            db.intervalQueries.selectById(id).executeAsOneOrNull()?.toDb()
        }

        suspend fun selectLastOneOrNull(): IntervalDb? = dbIo {
            db.intervalQueries.selectDesc(limit = 1).executeAsOneOrNull()?.toDb()
        }

        fun selectLastOneOrNullFlow(): Flow<IntervalDb?> = db.intervalQueries
            .selectDesc(limit = 1).asListFlow { toDb() }.map { it.lastOrNull() }

        //
        // Insert

        suspend fun insertWithValidation(
            timer: Int,
            activity: ActivityDb,
            note: String?,
            id: Int = time(),
        ): IntervalDb = dbIo {
            db.transactionWithResult {
                insertWithValidationNeedTransaction(
                    timer = timer,
                    activity = activity,
                    note = note,
                    id = id,
                )
            }
        }

        fun insertWithValidationNeedTransaction(
            timer: Int,
            activity: ActivityDb,
            note: String?,
            id: Int = time(),
        ): IntervalDb {
            db.intervalQueries.deleteById(id)
            val intervalSQ = IntervalSQ(
                id = id,
                timer = timer,
                activity_id = activity.id,
                note = note?.trim()?.takeIf { it.isNotBlank() },
            )
            db.intervalQueries.insert(intervalSQ)
            return intervalSQ.toDb()
        }

        ///

        suspend fun pauseLastInterval(): Unit = dbIo {

            db.transaction {

                val interval = db.intervalQueries.selectDesc(limit = 1).executeAsOne().toDb()
                val activity = interval.selectActivityDbCached()
                val paused: TextFeatures.Paused = run {
                    val intervalTf = (interval.note ?: "").textFeatures()
                    intervalTf.paused ?: run {
                        val originalTimer: Int = intervalTf.prolonged?.originalTimer ?: interval.timer
                        TextFeatures.Paused(interval.id, originalTimer)
                    }
                }
                val pausedTimer: Int = run {
                    val timeLeft = interval.id + interval.timer - time()
                    if (timeLeft > 0) timeLeft else paused.originalTimer
                }
                val pausedText = interval.note ?: activity.name
                val pausedTf = pausedText.textFeatures().copy(
                    activity = activity,
                    timer = pausedTimer,
                    paused = paused,
                    prolonged = null,
                )

                val pausedTaskId: Int = TaskDb.addWithValidation_transactionRequired(
                    text = pausedTf.textWithFeatures(),
                    folder = Cache.getTodayFolderDb(),
                )
                val pauseIntervalTf = "Break".textFeatures().copy(
                    pause = TextFeatures.Pause(pausedTaskId = pausedTaskId),
                )
                insertWithValidationNeedTransaction(
                    activity.pomodoro_timer,
                    ActivityDb.selectOtherCached(),
                    pauseIntervalTf.textWithFeatures(),
                )
            }
        }

        suspend fun prolongLastInterval(
            timer: Int,
        ): Unit = dbIo {
            val interval: IntervalDb = selectLastOneOrNull()!!
            val activityDb: ActivityDb = interval.selectActivityDb()
            val newTf: TextFeatures = run {
                val oldTf = (interval.note ?: activityDb.name).textFeatures()
                val prolonged = oldTf.prolonged ?: TextFeatures.Prolonged(interval.timer)
                oldTf.copy(prolonged = prolonged)
            }
            interval.up(
                timer = interval.timer + timer,
                note = newTf.textWithFeatures(),
                activityDb = activityDb,
            )
        }

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.intervalQueries.selectAsc(Int.MAX_VALUE.toLong()).asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.intervalQueries.insert(
                IntervalSQ(
                    id = j.getInt(0),
                    timer = j.getInt(1),
                    note = j.getStringOrNull(2),
                    activity_id = j.getInt(3),
                )
            )
        }
    }

    fun unixTime() = UnixTime(id)

    suspend fun selectActivityDb(): ActivityDb =
        ActivityDb.selectByIdOrNull(activity_id)!!

    fun selectActivityDbCached(): ActivityDb =
        Cache.getActivityDbByIdOrNull(activity_id)!!

    suspend fun updateActivity(newActivity: ActivityDb): Unit = dbIo {
        updateActivitySync(newActivity)
    }

    fun updateActivitySync(newActivity: ActivityDb) {
        db.intervalQueries.updateActivityIdById(
            id = id, activity_id = newActivity.id,
        )
    }

    suspend fun upId(newId: Int): Unit = dbIo {
        db.intervalQueries.updateId(
            oldId = id, newId = newId,
        )
    }

    suspend fun upTimer(timer: Int): Unit = dbIo {
        db.intervalQueries.updateTimerById(
            id = id,
            timer = timer,
        )
    }

    suspend fun up(
        timer: Int,
        note: String?,
        activityDb: ActivityDb,
    ): Unit = dbIo {
        db.intervalQueries.updateById(
            id = id,
            timer = timer,
            note = note,
            activity_id = activityDb.id,
        )
    }

    suspend fun delete(): Unit = dbIo {
        db.intervalQueries.deleteById(id)
    }

    //
    // Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, timer, note, activity_id,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.intervalQueries.updateById(
            id = j.getInt(0),
            timer = j.getInt(1),
            note = j.getStringOrNull(2),
            activity_id = j.getInt(3),
        )
    }

    override fun backupable__delete() {
        db.intervalQueries.deleteById(id)
    }
}

///

private fun IntervalSQ.toDb() = IntervalDb(
    id = id, timer = timer, note = note, activity_id = activity_id,
)
