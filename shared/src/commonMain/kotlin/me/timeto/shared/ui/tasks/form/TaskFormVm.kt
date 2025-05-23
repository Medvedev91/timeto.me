package me.timeto.shared.ui.tasks.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.ui.UiException
import me.timeto.shared.vm.__Vm

class TaskFormVm(
    val strategy: TaskFormStrategy,
) : __Vm<TaskFormVm.State>() {

    data class State(
        val title: String,
        val doneText: String,
        val textFeatures: TextFeatures,
    ) {

        val text: String =
            textFeatures.textNoFeatures
        val textPlaceholder = "Text"

        val activityDb: ActivityDb? = textFeatures.activity
        val activityTitle = "Activity"
        val activityNote: String =
            activityDb?.name?.textFeatures()?.textNoFeatures ?: "Not Selected"
        val activitiesUi: List<ActivityUi> =
            Cache.activitiesDbSorted.map { ActivityUi(it) }

        val timerSeconds: Int? = textFeatures.timer
        val timerSecondsPicker: Int = timerSeconds ?: (45 * 60)
        val timerTitle = "Timer"
        val timerNote: String =
            timerSeconds?.toTimerHintNote(isShort = false) ?: "Not Selected"

        val checklistsDb: List<ChecklistDb> = textFeatures.checklists
        val checklistsTitle = "Checklists"
        val checklistsNote: String =
            if (checklistsDb.isEmpty()) "None"
            else checklistsDb.joinToString(", ") { it.name }

        val shortcutsDb: List<ShortcutDb> = textFeatures.shortcuts
        val shortcutsTitle = "Shortcuts"
        val shortcutsNote: String =
            if (shortcutsDb.isEmpty()) "None"
            else shortcutsDb.joinToString(", ") { it.name }
    }

    override val state = MutableStateFlow(
        State(
            title = when (strategy) {
                is TaskFormStrategy.NewTask -> "New Task"
                is TaskFormStrategy.EditTask -> "Edit Task"
            },
            doneText = "Save",
            textFeatures = when (strategy) {
                is TaskFormStrategy.NewTask -> "".textFeatures()
                is TaskFormStrategy.EditTask -> strategy.taskDb.text.textFeatures()
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
            it.copy(textFeatures = it.textFeatures.copy(activity = activityDb))
        }
    }

    fun setTimer(seconds: Int) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(timer = seconds))
        }
    }

    fun setChecklists(checklistsDb: List<ChecklistDb>) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(checklists = checklistsDb))
        }
    }

    fun setShortcuts(shortcutsDb: List<ShortcutDb>) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(shortcuts = shortcutsDb))
        }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ): Unit = launchExIo {
        try {
            val tf: TextFeatures = state.value.textFeatures
            if (tf.textNoFeatures.isBlank())
                throw UiException("Empty text")
            val textWithFeatures: String =
                tf.textWithFeatures()
            when (strategy) {
                is TaskFormStrategy.NewTask -> {
                    TaskDb.addWithValidation(
                        text = textWithFeatures,
                        folder = strategy.taskFolderDb,
                    )
                }
                is TaskFormStrategy.EditTask -> {
                    strategy.taskDb.upTextWithValidation(
                        newText = textWithFeatures,
                    )
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

    data class ActivityUi(
        val activityDb: ActivityDb,
    ) {
        val title: String =
            activityDb.name.textFeatures().textNoFeatures
    }
}
