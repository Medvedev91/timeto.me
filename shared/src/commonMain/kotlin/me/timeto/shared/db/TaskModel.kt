package me.timeto.shared.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dbsq.TaskSQ
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import me.timeto.shared.*
import kotlin.math.max

data class TaskModel(
    val id: Int,
    val text: String,
    val folder_id: Int,
) : Backupable__Item {

    companion object : Backupable__Holder {

        fun anyChangeFlow() = db.taskQueries.anyChange().asFlow()

        suspend fun getAsc() = dbIO {
            db.taskQueries.getAsc().executeAsList().map { it.toModel() }
        }

        fun getAscFlow() = db.taskQueries.getAsc().asFlow()
            .mapToList().map { list -> list.map { it.toModel() } }

        suspend fun getByIdOrNull(id: Int): TaskModel? = dbIO {
            db.taskQueries.getById(id).executeAsOneOrNull()?.toModel()
        }

        ///
        /// Add

        suspend fun addWithValidation(
            text: String,
            folder: TaskFolderModel,
        ): Unit = dbIO {
            db.transaction {
                addWithValidation_transactionRequired(text = text, folder = folder)
            }
        }

        fun addWithValidation_transactionRequired(
            text: String,
            folder: TaskFolderModel,
        ) {
            db.taskQueries.insert(
                id = getNextId_ioRequired(),
                text = validateText(text),
                folder_id = folder.id
            )
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

            val activity = DI.activitiesSorted.firstOrNull { it.emoji in textFeatures.textNoFeatures }
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

        private fun TaskSQ.toModel() = TaskModel(
            id = id, text = text, folder_id = folder_id
        )

        ///
        /// Backupable Holder

        override fun backupable__getAll(): List<Backupable__Item> =
            db.taskQueries.getAsc().executeAsList().map { it.toModel() }

        override fun backupable__restore(json: JsonElement) {
            val j = json.jsonArray
            db.taskQueries.insert(
                id = j.getInt(0),
                text = j.getString(1),
                folder_id = j.getInt(2),
            )
        }
    }

    val isToday = folder_id == TaskFolderModel.ID_TODAY
    val isTmrw = folder_id == TaskFolderModel.ID_TMRW

    fun unixTime(utcOffset: Int = localUtcOffset) = UnixTime(id, utcOffset = utcOffset)

    suspend fun startInterval(
        deadline: Int,
        activity: ActivityModel,
        intervalId: Int = time(),
    ) = dbIO {
        db.transaction {
            IntervalModel.addWithValidationNeedTransaction(
                deadline = deadline,
                activity = activity,
                note = text.textFeatures().copy(isPaused = false).textWithFeatures(),
                id = intervalId,
            )
            db.taskQueries.deleteById(id)
        }
    }

    fun startIntervalForUI(
        onStarted: () -> Unit,
        needSheet: () -> Unit, // todo data for sheet
    ) {
        val autostartData = taskAutostartData(this) ?: return needSheet()
        launchExDefault {
            startInterval(
                deadline = autostartData.second,
                activity = autostartData.first,
            )
            onStarted()
        }
    }

    suspend fun upTextWithValidation(newText: String): Unit = dbIO {
        db.taskQueries.upTextById(
            id = id, text = validateText(newText)
        )
    }

    suspend fun upFolder(
        newFolder: TaskFolderModel,
        replaceIfTmrw: Boolean,
    ): Unit = dbIO {
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

    suspend fun delete(): Unit = dbIO {
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
