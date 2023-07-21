package me.timeto.shared.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import dbsq.IntervalSQ
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*

data class IntervalModel(
    val id: Int,
    val deadline: Int,
    val note: String?,
    val activity_id: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        const val HOT_INTERVALS_LIMIT = 200 // todo 200? Remember limit for WatchToIosSync

        const val DEADLINE_AFTER_PAUSE = 5 * 60

        fun anyChangeFlow() = db.intervalQueries.anyChange().asFlow()

        suspend fun getCount(): Int = dbIO {
            db.intervalQueries.getCount().executeAsOne().toInt()
        }

        ///
        /// Select many

        suspend fun getAsc(limit: Int = Int.MAX_VALUE) = dbIO {
            db.intervalQueries.getAsc(limit = limit.toLong()).executeAsList().map { it.toModel() }
        }

        fun getAscFlow(limit: Int = Int.MAX_VALUE) = db.intervalQueries.getAsc(limit = limit.toLong())
            .asFlow().mapToList().map { list -> list.map { it.toModel() } }

        suspend fun getDesc(limit: Int): List<IntervalModel> = dbIO {
            db.intervalQueries.getDesc(limit.toLong()).executeAsList().map { it.toModel() }
        }

        fun getDescFlow(limit: Int = Int.MAX_VALUE) = db.intervalQueries.getDesc(limit.toLong()).asFlow()
            .mapToList().map { list -> list.map { it.toModel() } }

        suspend fun getBetweenIdDesc(
            timeStart: Int,
            timeFinish: Int,
            limit: Int = Int.MAX_VALUE,
        ) = dbIO {
            db.intervalQueries.getBetweenIdDesc(
                timeStart = timeStart, timeFinish = timeFinish, limit = limit.toLong()
            )
                .executeAsList()
                .map { it.toModel() }
        }

        fun getFirstAndLastNeedTransaction() = listOf(
            db.intervalQueries.getAsc(limit = 1).executeAsOne().toModel(),
            db.intervalQueries.getDesc(limit = 1).executeAsOne().toModel(),
        )

        ///
        /// Select One

        suspend fun getByIdOrNull(id: Int): IntervalModel? = dbIO {
            db.intervalQueries.getById(id).executeAsOneOrNull()?.toModel()
        }

        suspend fun getLastOneOrNull(): IntervalModel? = dbIO {
            db.intervalQueries.getDesc(limit = 1).executeAsOneOrNull()?.toModel()
        }

        fun getLastOneOrNullFlow() = db.intervalQueries.getDesc(limit = 1).asFlow()
            .mapToOneOrNull().map { it?.toModel() }

        ///
        /// Add

        suspend fun addWithValidation(
            deadline: Int,
            activity: ActivityModel,
            note: String?,
            id: Int = time(),
        ): IntervalModel = dbIO {
            db.transactionWithResult {
                addWithValidationNeedTransaction(
                    deadline = deadline,
                    activity = activity,
                    note = note,
                    id = id,
                )
            }
        }

        fun addWithValidationNeedTransaction(
            deadline: Int,
            activity: ActivityModel,
            note: String?,
            id: Int = time(),
        ): IntervalModel {
            db.intervalQueries.deleteById(id)
            val intervalSQ = IntervalSQ(
                id = id,
                deadline = deadline,
                activity_id = activity.id,
                note = note?.trim()?.takeIf { it.isNotBlank() },
            )
            db.intervalQueries.insert(intervalSQ)
            return intervalSQ.toModel()
        }

        //////

        // todo rename to last
        // todo use transaction
        suspend fun restartActualInterval() {
            val interval = getLastOneOrNull()!!
            addWithValidation(
                interval.deadline,
                interval.getActivityDI(),
                interval.note
            )
        }

        suspend fun pauseLastInterval(): Unit = dbIO {
            db.transaction {
                val lastInterval = db.intervalQueries.getDesc(limit = 1).executeAsOne().toModel()
                val activity = lastInterval.getActivityDI()
                val timer: Int? = run {
                    val timeLeft = lastInterval.id + lastInterval.deadline - time()
                    if (timeLeft > 0) timeLeft else null
                }
                val text = lastInterval.note ?: activity.name
                val tf = text.textFeatures().copy(
                    activity = activity,
                    timer = timer,
                    isPaused = true,
                )
                TaskModel.addWithValidation_transactionRequired(
                    text = tf.textWithFeatures(),
                    folder = DI.getTodayFolder(),
                )
                addWithValidationNeedTransaction(DEADLINE_AFTER_PAUSE, ActivityModel.getOther(), null)
            }
        }

        private fun IntervalSQ.toModel() = IntervalModel(
            id = id, deadline = deadline, note = note, activity_id = activity_id
        )

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.intervalQueries.getAsc(Int.MAX_VALUE.toLong()).executeAsList().map { it.toModel() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.intervalQueries.insert(
                IntervalSQ(
                    id = j.getInt(0),
                    deadline = j.getInt(1),
                    note = j.getStringOrNull(2),
                    activity_id = j.getInt(3),
                )
            )
        }
    }

    fun unixTime() = UnixTime(id)

    fun getTriggers(): List<TextFeatures.Trigger> {
        val triggers = mutableListOf<TextFeatures.Trigger>()
        // Priority to note
        if (note != null)
            triggers.addAll(note.textFeatures().triggers)
        triggers.addAll(getActivityDI().name.textFeatures().triggers)
        return triggers
    }

    fun getActivityDI() = DI.activitiesSorted.first { it.id == activity_id }

    suspend fun upActivity(newActivity: ActivityModel): Unit = dbIO {
        db.intervalQueries.upActivityIdById(
            id = id, activity_id = newActivity.id
        )
    }

    suspend fun upId(newId: Int): Unit = dbIO {
        db.intervalQueries.upId(
            oldId = id, newId = newId
        )
    }

    suspend fun delete(): Unit = dbIO {
        db.intervalQueries.deleteById(id)
    }

    ///
    /// Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, deadline, note, activity_id
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.intervalQueries.upById(
            id = j.getInt(0),
            deadline = j.getInt(1),
            note = j.getStringOrNull(2),
            activity_id = j.getInt(3),
        )
    }

    override fun backupable__delete() {
        db.intervalQueries.deleteById(id)
    }
}
