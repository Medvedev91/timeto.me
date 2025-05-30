package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.TaskSQ
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.Cache
import me.timeto.shared.TextFeatures
import me.timeto.shared.TimerTimeParser
import me.timeto.shared.UnixTime
import me.timeto.shared.launchExIo
import me.timeto.shared.localUtcOffset
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.misc.getInt
import me.timeto.shared.misc.getString
import me.timeto.shared.time
import me.timeto.shared.misc.toJsonArray
import me.timeto.shared.textFeatures
import me.timeto.shared.ui.UiException
import me.timeto.shared.ui.tasks.TaskUi
import kotlin.math.max

data class TaskDb(
    val id: Int,
    val text: String,
    val folder_id: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow(): Flow<*> =
            db.taskQueries.anyChange().asFlow()

        suspend fun selectAsc(): List<TaskDb> = dbIo {
            db.taskQueries.selectAsc().asList { toDb() }
        }

        fun selectAscFlow(): Flow<List<TaskDb>> =
            db.taskQueries.selectAsc().asListFlow { toDb() }

        suspend fun selectByIdOrNull(id: Int): TaskDb? = dbIo {
            db.taskQueries.selectById(id).executeAsOneOrNull()?.toDb()
        }

        //
        // Insert

        suspend fun insertWithValidation(
            text: String,
            folder: TaskFolderDb,
        ): Unit = dbIo {
            db.transaction {
                insertWithValidation_transactionRequired(
                    text = text,
                    folder = folder,
                )
            }
        }

        fun insertWithValidation_transactionRequired(
            text: String,
            folder: TaskFolderDb,
        ): Int {
            val newId = getNextId_ioRequired()
            db.taskQueries.insert(
                id = newId,
                text = validateText(text),
                folder_id = folder.id
            )
            return newId
        }

        private fun getNextId_ioRequired(): Int = max(
            time(),
            db.taskQueries.selectDesc(limit = 1).executeAsOneOrNull()?.id?.plus(1) ?: 0
        )

        //////

        private fun validateText(textToValidate: String): String {
            var textFeatures = textToValidate.textFeatures()

            val timeParser = TimerTimeParser.parse(textFeatures.textNoFeatures)
            if (timeParser != null) {
                textFeatures = textFeatures.copy(
                    timer = timeParser.seconds,
                    textNoFeatures = textFeatures.textNoFeatures.replace(timeParser.match, ""),
                )
            }

            val activity = Cache.activitiesDbSorted.firstOrNull { it.emoji in textFeatures.textNoFeatures }
            if (activity != null) {
                textFeatures = textFeatures.copy(
                    activity = activity,
                    textNoFeatures = textFeatures.textNoFeatures.replace(activity.emoji, "")
                )
            }

            val validatedText = textFeatures.textWithFeatures()
            if (validatedText.isEmpty())
                throw UiException("Empty text")
            return validatedText
        }

        //
        // Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.taskQueries.selectAsc().asList { toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.taskQueries.insert(
                id = j.getInt(0),
                text = j.getString(1),
                folder_id = j.getInt(2),
            )
        }
    }

    val isToday: Boolean = folder_id == TaskFolderDb.ID_TODAY
    val isTmrw: Boolean = folder_id == TaskFolderDb.ID_TMRW

    fun toUi() = TaskUi(this)

    fun unixTime(utcOffset: Int = localUtcOffset) =
        UnixTime(id, utcOffset = utcOffset)

    suspend fun startInterval(
        timer: Int,
        activityDb: ActivityDb,
        intervalId: Int = time(),
    ): Unit = dbIo {
        db.transaction {
            IntervalDb.insertWithValidationNeedTransaction(
                timer = timer,
                activityDb = activityDb,
                note = text,
                id = intervalId,
            )
            db.taskQueries.deleteById(id)
        }
    }

    fun startIntervalForUi(
        ifJustStarted: () -> Unit,
        ifActivityNeeded: () -> Unit,
        ifTimerNeeded: (ActivityDb) -> Unit,
    ) {
        val tf: TextFeatures = this.text.textFeatures()
        val activityDb: ActivityDb? = tf.activity
        val seconds: Int? = tf.timer

        if (activityDb != null && seconds != null) {
            launchExIo {
                startInterval(
                    timer = seconds,
                    activityDb = activityDb,
                )
                ifJustStarted()
            }
            return
        }

        if (activityDb != null) {
            ifTimerNeeded(activityDb)
            return
        }

        ifActivityNeeded()
    }

    suspend fun updateTextWithValidation(newText: String): Unit = dbIo {
        db.taskQueries.updateTextById(
            id = id, text = validateText(newText)
        )
    }

    suspend fun updateFolder(
        newFolder: TaskFolderDb,
        replaceIfTmrw: Boolean,
    ): Unit = dbIo {
        db.taskQueries.updateFolderIdById(
            id = id,
            folder_id = newFolder.id,
        )
        // To know which day the task moved to "Tomorrow"
        if (replaceIfTmrw && newFolder.isTmrw) {
            db.taskQueries.updateId(
                oldId = id,
                newId = getNextId_ioRequired(),
            )
        }
    }

    suspend fun delete(): Unit = dbIo {
        db.taskQueries.deleteById(id)
    }

    //
    // Backupable Item

    override fun backupable__getId(): String =
        id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, text, folder_id
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.taskQueries.updateById(
            id = j.getInt(0),
            text = j.getString(1),
            folder_id = j.getInt(2),
        )
    }

    override fun backupable__delete() {
        db.taskQueries.deleteById(id)
    }
}

private fun TaskSQ.toDb() = TaskDb(
    id = id, text = text, folder_id = folder_id,
)
