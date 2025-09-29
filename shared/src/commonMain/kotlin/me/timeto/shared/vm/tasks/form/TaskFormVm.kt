package me.timeto.shared.vm.tasks.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.vm.Vm

class TaskFormVm(
    val strategy: TaskFormStrategy,
) : Vm<TaskFormVm.State>() {

    data class State(
        val title: String,
        val doneText: String,
        val textFeatures: TextFeatures,
    ) {

        val text: String =
            textFeatures.textNoFeatures
        val textPlaceholder = "Text"

        val goalDb: Goal2Db? = textFeatures.goalDb
        val goalTitle = "Goal"
        val goalNote: String =
            goalDb?.name?.textFeatures()?.textNoFeatures ?: "Not Selected"
        val goalsUi: List<GoalUi> =
            Cache.goals2Db.map { GoalUi(it) }

        val timerSeconds: Int? = textFeatures.timer
        val timerSecondsPicker: Int = timerSeconds ?: (45 * 60)
        val timerTitle = "Timer"
        val timerNote: String =
            timerSeconds?.toTimerHintNote(isShort = false) ?: "Not Selected"

        val checklistsDb: List<ChecklistDb> = textFeatures.checklistsDb
        val checklistsTitle = "Checklists"
        val checklistsNote: String =
            if (checklistsDb.isEmpty()) "None"
            else checklistsDb.joinToString(", ") { it.name }

        val shortcutsDb: List<ShortcutDb> = textFeatures.shortcutsDb
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

    fun setGoal(goalDb: Goal2Db?) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(goalDb = goalDb))
        }
    }

    fun setTimer(seconds: Int) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(timer = seconds))
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
            val tf: TextFeatures = state.value.textFeatures
            if (tf.textNoFeatures.isBlank())
                throw UiException("Empty text")
            val textWithFeatures: String =
                tf.textWithFeatures()
            when (strategy) {
                is TaskFormStrategy.NewTask -> {
                    TaskDb.insertWithValidation(
                        text = textWithFeatures,
                        folder = strategy.taskFolderDb,
                    )
                }
                is TaskFormStrategy.EditTask -> {
                    strategy.taskDb.updateTextWithValidation(
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

    data class GoalUi(
        val goalDb: Goal2Db,
    ) {
        val title: String =
            goalDb.name.textFeatures().textNoFeatures
    }
}
