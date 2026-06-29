package me.timeto.shared.vm.task_form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.ActivityUi
import me.timeto.shared.Cache
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.DialogsManager
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.TaskFolderUi
import me.timeto.shared.UiException
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.vm.Vm

class TaskFormVm(
    val strategy: TaskFormStrategy,
) : Vm<TaskFormVm.State>() {

    data class State(
        val title: String,
        val doneText: String,
        val textFeatures: TextFeatures,
        val settingsLogic: SettingsLogic,
    ) {

        val text: String =
            textFeatures.textNoFeatures
        val textPlaceholder = "Text"

        val activityDb: ActivityDb? =
            textFeatures.activityDb

        val timerSeconds: Int? = when (val timerType = textFeatures.timerType) {
            is TextFeatures.TimerType.Timer -> timerType.seconds
            is TextFeatures.TimerType.OverdueTimer -> null
            is TextFeatures.TimerType.Stopwatch -> null
            null -> null
        }
        val timerSecondsPicker: Int = timerSeconds ?: (45 * 60)
        val timerTitle = "Timer"
        val timerNote: String =
            timerSeconds?.toTimerHintNote(isShort = false) ?: "Not Selected"

        val checklistsDb: List<ChecklistDb> =
            textFeatures.checklistsDb
        val checklistsTitle = "Checklists"
        val checklistsNote: String =
            if (checklistsDb.isEmpty()) "None"
            else checklistsDb.joinToString(", ") { it.name }

        val shortcutsDb: List<ShortcutDb> =
            textFeatures.shortcutsDb
        val shortcutsTitle = "Shortcuts"
        val shortcutsNote: String =
            if (shortcutsDb.isEmpty()) "None"
            else shortcutsDb.joinToString(", ") { it.name }
    }

    override val state: MutableStateFlow<State> = MutableStateFlow(
        State(
            title = when (strategy) {
                is TaskFormStrategy.NewTask -> "New Task"
                is TaskFormStrategy.EditTask -> "Edit Task"
            },
            doneText = "Save",
            textFeatures = when (strategy) {
                is TaskFormStrategy.NewTask -> "".textFeatures().copy(
                    activityDb = strategy.activityDb,
                )
                is TaskFormStrategy.EditTask ->
                    strategy.taskDb.text.textFeatures()
            },
            settingsLogic = run {
                val taskFolderDb: TaskFolderDb = when (strategy) {
                    is TaskFormStrategy.NewTask -> strategy.taskFolderDb
                    is TaskFormStrategy.EditTask -> strategy.taskDb.selectTaskFolderDbCached()
                }
                val taskFolderActivityDb: ActivityDb? =
                    taskFolderDb.selectActivityDbOrNullCached()
                if (taskFolderActivityDb != null) {
                    SettingsLogic.FixedTaskFolderUi(
                        taskFolderDb = taskFolderDb,
                        selectedHintUi = when {
                            taskFolderDb.isToday ->
                                SettingsLogic.FixedTaskFolderUi.TaskFolderHintUi.today
                            taskFolderDb.isTomorrow ->
                                SettingsLogic.FixedTaskFolderUi.TaskFolderHintUi.tomorrow
                            else ->
                                null
                        },
                    )
                } else {
                    SettingsLogic.ActivitiesUi(
                        activitiesUi = Cache.activitiesDb.activitiesUiSorted(),
                    )
                }
            },
        )
    )

    fun setText(text: String) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
        }
    }

    fun setActivity(activityDb: ActivityDb?) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(activityDb = activityDb))
        }
    }

    fun setSessionLogic(sessionLogic: SettingsLogic) {
        state.update { it.copy(settingsLogic = sessionLogic) }
    }

    fun setTimer(seconds: Int) {
        state.update {
            it.copy(
                textFeatures = it.textFeatures.copy(
                    timerType = TextFeatures.TimerType.Timer(seconds),
                )
            )
        }
    }

    fun setChecklists(checklistsDb: List<ChecklistDb>) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(checklistsDb = checklistsDb))
        }
    }

    fun setShortcuts(shortcutsDb: List<ShortcutDb>) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(shortcutsDb = shortcutsDb))
        }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ): Unit = launchExIo {
        try {
            val tf: TextFeatures =
                state.value.textFeatures
            if (tf.textNoFeatures.isBlank())
                throw UiException("Empty text")
            val textWithFeatures: String =
                tf.textWithFeatures()
            val settingsLogic: SettingsLogic =
                state.value.settingsLogic
            when (strategy) {
                is TaskFormStrategy.NewTask -> {
                    val taskFolderDb: TaskFolderDb = when (settingsLogic) {
                        is SettingsLogic.FixedTaskFolderUi ->
                            settingsLogic.selectedHintUi?.taskFolderUi?.taskFolderDb ?: settingsLogic.taskFolderDb
                        is SettingsLogic.ActivitiesUi ->
                            strategy.taskFolderDb
                    }
                    TaskDb.insertWithValidation(
                        text = textWithFeatures,
                        folder = taskFolderDb,
                    )
                }
                is TaskFormStrategy.EditTask -> {
                    val taskDb: TaskDb = strategy.taskDb
                    taskDb.updateTextWithValidation(
                        newText = textWithFeatures,
                    )
                    if (settingsLogic is SettingsLogic.FixedTaskFolderUi) {
                        val hintTaskFolderDb: TaskFolderDb? =
                            settingsLogic.selectedHintUi?.taskFolderUi?.taskFolderDb
                        if (hintTaskFolderDb != null && hintTaskFolderDb.id != taskDb.id) {
                            taskDb.updateFolder(
                                taskFolderDb = hintTaskFolderDb,
                                updateFolderActivity = true, // No matter for today/tomorrow
                                replaceIfTmrw = true,
                            )
                        }
                    }
                }
            }
            onUi {
                onSuccess()
            }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    fun delete(
        taskDb: TaskDb,
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        val text: String =
            taskDb.text.textFeatures().textNoFeatures
        dialogsManager.confirmation(
            message = "Remove \"$text\"?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    taskDb.delete()
                    onUi {
                        onSuccess()
                    }
                }
            },
        )
    }

    ///

    sealed class SettingsLogic {

        data class FixedTaskFolderUi(
            val taskFolderDb: TaskFolderDb,
            val selectedHintUi: TaskFolderHintUi?,
        ) : SettingsLogic() {

            val title: String = run {
                val activityDb: ActivityDb? =
                    taskFolderDb.selectActivityDbOrNullCached()
                if (activityDb != null)
                    return@run activityDb.name.textFeatures().textNoFeatures
                taskFolderDb.name
            }

            val taskFolderHintsUi: List<TaskFolderHintUi> = listOf(
                TaskFolderHintUi.today,
                TaskFolderHintUi.tomorrow,
            )

            fun buildWithNewHint(hintUi: TaskFolderHintUi): FixedTaskFolderUi =
                copy(
                    selectedHintUi =
                        if (hintUi.taskFolderUi.taskFolderDb.id == selectedHintUi?.taskFolderUi?.taskFolderDb?.id) null
                        else hintUi
                )

            ///

            enum class TaskFolderHintUi(
                val taskFolderUi: TaskFolderUi,
            ) {
                today(TaskFolderUi(Cache.todayTaskFolderDb, null)),
                tomorrow(TaskFolderUi(Cache.tomorrowTaskFolderDb, null))
            }
        }

        data class ActivitiesUi(
            val activitiesUi: List<ActivityUi>,
        ) : SettingsLogic()
    }
}

private fun List<ActivityDb>.activitiesUiSorted(): List<ActivityUi> = this
    .map {
        it to (HomeButtonSort.parseOrNull(it.home_button_sort) ?: HomeButtonSort(0, 0, 0))
    }
    .sortedWith(
        compareBy({ it.second.rowIdx }, { it.second.cellIdx })
    )
    .map { ActivityUi(it.first) }
