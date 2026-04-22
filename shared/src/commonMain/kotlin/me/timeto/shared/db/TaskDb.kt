package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import dbsq.TaskSq
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.TextFeatures
import me.timeto.shared.TimerTimeParser
import me.timeto.shared.launchExIo
import me.timeto.shared.backups.Backupable__Holder
import me.timeto.shared.backups.Backupable__Item
import me.timeto.shared.getInt
import me.timeto.shared.getString
import me.timeto.shared.time
import me.timeto.shared.toJsonArray
import me.timeto.shared.textFeatures
import me.timeto.shared.UiException
import me.timeto.shared.TaskUi
import me.timeto.shared.toBoolean10
import me.timeto.shared.toInt10
import kotlin.math.max

data class TaskDb(
    val id: Int,
    val folder_id: Int,
    val onHomeActivity: Boolean,
    val text: String,
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
            onHomeActivity: Boolean,
            folder: TaskFolderDb,
        ): Unit = dbIo {
            db.transaction {
                insertWithValidation_transactionRequired(
                    folder = folder,
                    onHomeActivity = onHomeActivity,
                    text = text,
                )
            }
        }

        fun insertWithValidation_transactionRequired(
            folder: TaskFolderDb,
            onHomeActivity: Boolean,
            text: String,
        ): Int {
            val newId = getNextId_ioRequired()
            db.taskQueries.insert(
                id = newId,
                folder_id = folder.id,
                on_home_activity = onHomeActivity.toInt10(),
                text = validateText(text),
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
                    timerType = TextFeatures.TimerType.Timer(seconds = timeParser.seconds),
                    textNoFeatures = textFeatures.textNoFeatures.replace(timeParser.match, ""),
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
                folder_id = j.getInt(1),
                on_home_activity = j.getInt(2),
                text = j.getString(3),
            )
        }
    }

    val isToday: Boolean = folder_id == TaskFolderDb.ID_TODAY
    val isTmrw: Boolean = folder_id == TaskFolderDb.ID_TMRW

    fun toUi() = TaskUi(this)

    suspend fun startInterval(
        tfTimerType: TextFeatures.TimerType,
        activityDb: ActivityDb,
        intervalId: Int = time(),
    ): Unit = dbIo {
        db.transaction {
            IntervalDb.insertWithValidationNeedTransaction(
                activityDb = activityDb,
                note = text.textFeatures().copy(timerType = tfTimerType).textWithFeatures(),
                id = intervalId,
            )
            db.taskQueries.deleteById(id)
        }
    }

    suspend fun startTimer(
        seconds: Int,
        activityDb: ActivityDb,
    ): Unit = dbIo {
        startInterval(
            tfTimerType = TextFeatures.TimerType.Timer(seconds = seconds),
            activityDb = activityDb,
        )
    }

    fun startIntervalForUi(
        ifJustStarted: () -> Unit,
        ifTimerNeeded: () -> Unit,
    ) {
        val tf: TextFeatures = this.text.textFeatures()
        val activityDb: ActivityDb? = tf.activityDb
        val tfTimerType: TextFeatures.TimerType? = tf.timerType

        if (activityDb != null && tfTimerType != null) {
            launchExIo {
                startInterval(
                    tfTimerType = tfTimerType,
                    activityDb = activityDb,
                )
                ifJustStarted()
            }
            return
        }

        ifTimerNeeded()
    }

    suspend fun toggleOnHomeActivity(): Unit = dbIo {
        db.taskQueries.updateOnHomeActivityById(
            on_home_activity = onHomeActivity.not().toInt10(),
            id = id,
        )
    }

    suspend fun updateTextWithValidation(newText: String): Unit = dbIo {
        db.taskQueries.updateTextById(
            id = id, text = validateText(newText)
        )
    }

    suspend fun updateFolder(
        taskFolderDb: TaskFolderDb,
        updateFolderActivity: Boolean,
        replaceIfTmrw: Boolean,
    ): Unit = dbIo {
        db.transaction {
            db.taskQueries.updateFolderIdById(
                id = id,
                folder_id = taskFolderDb.id,
            )
            if (updateFolderActivity) {
                val activityDb: ActivityDb? =
                    taskFolderDb.selectActivityDbOrNullCached()
                if (activityDb != null)
                    db.taskQueries.updateTextById(
                        id = id,
                        text = text.textFeatures().copy(activityDb = activityDb).textWithFeatures(),
                    )
            }
            // To know which day the task moved to "Tomorrow"
            if (replaceIfTmrw && taskFolderDb.isTmrw) {
                db.taskQueries.updateId(
                    oldId = id,
                    newId = getNextId_ioRequired(),
                )
            }
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
        id, folder_id, onHomeActivity.toInt10(), text,
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.taskQueries.updateById(
            id = j.getInt(0),
            folder_id = j.getInt(1),
            on_home_activity = j.getInt(2),
            text = j.getString(3),
        )
    }

    override fun backupable__delete() {
        db.taskQueries.deleteById(id)
    }
}

private fun TaskSq.toDb() = TaskDb(
    id = id,
    folder_id = folder_id,
    onHomeActivity = on_home_activity.toBoolean10(),
    text = text,
)
