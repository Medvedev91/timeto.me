package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dbsq.IntervalSQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*

data class IntervalDb(
    val id: Int,
    val timer: Int,
    val note: String?,
    val activity_id: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        const val HOT_INTERVALS_LIMIT = 200 // todo 200? Remember limit for WatchToIosSync

        fun anyChangeFlow() = db.intervalQueries.anyChange().asFlow()

        suspend fun getCount(): Int = dbIo {
            db.intervalQueries.getCount().executeAsOne().toInt()
        }

        ///
        /// Select many

        suspend fun getAsc(limit: Int = Int.MAX_VALUE) = dbIo {
            db.intervalQueries.getAsc(limit = limit.toLong()).executeAsList().map { it.toDb() }
        }

        fun getAscFlow(limit: Int = Int.MAX_VALUE) = db.intervalQueries.getAsc(limit = limit.toLong())
            .asFlow().mapToList(Dispatchers.IO).map { list -> list.map { it.toDb() } }

        suspend fun getDesc(limit: Int): List<IntervalDb> = dbIo {
            db.intervalQueries.getDesc(limit.toLong()).executeAsList().map { it.toDb() }
        }

        fun getDescFlow(limit: Int = Int.MAX_VALUE) = db.intervalQueries.getDesc(limit.toLong()).asFlow()
            .mapToList(Dispatchers.IO).map { list -> list.map { it.toDb() } }

        suspend fun getBetweenIdDesc(
            timeStart: Int,
            timeFinish: Int,
            limit: Int = Int.MAX_VALUE,
        ) = dbIo {
            db.intervalQueries.getBetweenIdDesc(
                timeStart = timeStart, timeFinish = timeFinish, limit = limit.toLong()
            )
                .executeAsList()
                .map { it.toDb() }
        }

        fun getFirstAndLastNeedTransaction() = listOf(
            db.intervalQueries.getAsc(limit = 1).executeAsOne().toDb(),
            db.intervalQueries.getDesc(limit = 1).executeAsOne().toDb(),
        )

        ///
        /// Select One

        suspend fun getByIdOrNull(id: Int): IntervalDb? = dbIo {
            db.intervalQueries.getById(id).executeAsOneOrNull()?.toDb()
        }

        suspend fun getLastOneOrNull(): IntervalDb? = dbIo {
            db.intervalQueries.getDesc(limit = 1).executeAsOneOrNull()?.toDb()
        }

        fun getLastOneOrNullFlow() = db.intervalQueries.getDesc(limit = 1).asFlow()
            .mapToOneOrNull(Dispatchers.IO).map { it?.toDb() }

        ///
        /// Add

        suspend fun addWithValidation(
            timer: Int,
            activity: ActivityDb,
            note: String?,
            id: Int = time(),
        ): IntervalDb = dbIo {
            db.transactionWithResult {
                addWithValidationNeedTransaction(
                    timer = timer,
                    activity = activity,
                    note = note,
                    id = id,
                )
            }
        }

        fun addWithValidationNeedTransaction(
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

        //////

        suspend fun pauseLastInterval(): Unit = dbIo {

            db.transaction {

                val interval = db.intervalQueries.getDesc(limit = 1).executeAsOne().toDb()
                val activity = interval.getActivityDbCached()
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
                    folder = Cache.getTodayFolder(),
                )
                val pauseIntervalTf = "Break".textFeatures().copy(
                    pause = TextFeatures.Pause(pausedTaskId = pausedTaskId),
                )
                addWithValidationNeedTransaction(
                    activity.pomodoro_timer,
                    ActivityDb.getOther(),
                    pauseIntervalTf.textWithFeatures(),
                )
            }
        }

        suspend fun prolongLastInterval(
            timer: Int,
        ): Unit = dbIo {
            val interval = getLastOneOrNull()!!
            val activityDb = interval.getActivity()
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
            db.intervalQueries.getAsc(Int.MAX_VALUE.toLong()).executeAsList().map { it.toDb() }

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

    suspend fun getActivity(): ActivityDb = ActivityDb.getByIdOrNull(activity_id)!!

    fun getActivityDbCached(): ActivityDb =
        Cache.activitiesSorted.first { it.id == activity_id }

    suspend fun upActivity(newActivity: ActivityDb): Unit = dbIo {
        db.intervalQueries.upActivityIdById(
            id = id, activity_id = newActivity.id
        )
    }

    suspend fun upId(newId: Int): Unit = dbIo {
        db.intervalQueries.upId(
            oldId = id, newId = newId
        )
    }

    suspend fun upTimer(timer: Int): Unit = dbIo {
        db.intervalQueries.upTimerById(
            id = id,
            timer = timer,
        )
    }

    suspend fun up(
        timer: Int,
        note: String?,
        activityDb: ActivityDb,
    ): Unit = dbIo {
        db.intervalQueries.upById(
            id = id,
            timer = timer,
            note = note,
            activity_id = activityDb.id,
        )
    }

    suspend fun delete(): Unit = dbIo {
        db.intervalQueries.deleteById(id)
    }

    ///
    /// Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, timer, note, activity_id
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.intervalQueries.upById(
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

private fun IntervalSQ.toDb() = IntervalDb(
    id = id, timer = timer, note = note, activity_id = activity_id,
)
