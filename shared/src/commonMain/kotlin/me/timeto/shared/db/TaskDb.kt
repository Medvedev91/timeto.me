package me.timeto.shared.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dbsq.TaskSQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*
import me.timeto.shared.models.TaskUi
import kotlin.math.max

data class TaskDb(
    val id: Int,
    val text: String,
    val folder_id: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow() = db.taskQueries.anyChange().asFlow()

        suspend fun getAsc() = dbIo {
            db.taskQueries.getAsc().executeAsList().map { it.toDb() }
        }

        fun getAscFlow() = db.taskQueries.getAsc().asFlow()
            .mapToList(Dispatchers.IO).map { list -> list.map { it.toDb() } }

        suspend fun getByIdOrNull(id: Int): TaskDb? = dbIo {
            db.taskQueries.getById(id).executeAsOneOrNull()?.toDb()
        }

        ///
        /// Add

        suspend fun addWithValidation(
            text: String,
            folder: TaskFolderDb,
        ): Unit = dbIo {
            db.transaction {
                addWithValidation_transactionRequired(text = text, folder = folder)
            }
        }

        fun addWithValidation_transactionRequired(
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
            db.taskQueries.getDesc(limit = 1).executeAsOneOrNull()?.id?.plus(1) ?: 0
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
                throw UIException("Empty text")
            return validatedText
        }

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.taskQueries.getAsc().executeAsList().map { it.toDb() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.taskQueries.insert(
                id = j.getInt(0),
                text = j.getString(1),
                folder_id = j.getInt(2),
            )
        }
    }

    val isToday = folder_id == TaskFolderDb.ID_TODAY
    val isTmrw = folder_id == TaskFolderDb.ID_TMRW

    fun toUi() = TaskUi(this)

    fun unixTime(utcOffset: Int = localUtcOffset) = UnixTime(id, utcOffset = utcOffset)

    suspend fun startInterval(
        timer: Int,
        activity: ActivityDb,
        intervalId: Int = time(),
    ) = dbIo {
        db.transaction {
            IntervalDb.addWithValidationNeedTransaction(
                timer = timer,
                activity = activity,
                note = text,
                id = intervalId,
            )
            db.taskQueries.deleteById(id)
        }
    }

    fun startIntervalForUI(
        onStarted: () -> Unit,
        activitiesSheet: () -> Unit, // todo data for sheet
        timerSheet: (activity: ActivityDb) -> Unit,
    ) {
        val tf = this.text.textFeatures()
        val (activity, timer) = tf.activity to tf.timer

        if (activity != null && timer != null) {
            launchExDefault {
                startInterval(
                    timer = timer,
                    activity = activity,
                )
                onStarted()
            }
            return
        }

        if (activity != null) {
            timerSheet(activity)
            return
        }

        activitiesSheet()
    }

    suspend fun upTextWithValidation(newText: String): Unit = dbIo {
        db.taskQueries.upTextById(
            id = id, text = validateText(newText)
        )
    }

    suspend fun upFolder(
        newFolder: TaskFolderDb,
        replaceIfTmrw: Boolean,
    ): Unit = dbIo {
        db.taskQueries.upFolderIdById(
            id = id,
            folder_id = newFolder.id,
        )
        // To know which day the task moved to "Tomorrow"
        if (replaceIfTmrw && newFolder.isTmrw) {
            db.taskQueries.upId(
                oldId = id,
                newId = getNextId_ioRequired(),
            )
        }
    }

    suspend fun delete(): Unit = dbIo {
        db.taskQueries.deleteById(id)
    }

    ///
    /// Backupable Item

    override fun backupable__getId(): String = id.toString()

    override fun backupable__backup(): JsonElement = listOf(
        id, text, folder_id
    ).toJsonArray()

    override fun backupable__update(json: JsonElement) {
        val j = json.jsonArray
        db.taskQueries.upById(
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
