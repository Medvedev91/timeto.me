package me.timeto.shared.ui.repeatings.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.launchExIo
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.vm.__Vm

class RepeatingFormVm(
    initRepeatingDb: RepeatingDb?,
) : __Vm<RepeatingFormVm.State>() {

    data class State(
        val title: String,
        val saveText: String,
        val text: String,
    ) {

        val textPlaceholder = "Task"
    }

    override val state = MutableStateFlow(
        State(
            title = if (initRepeatingDb != null) "Edit Repeating" else "New Repeating",
            saveText = if (initRepeatingDb != null) "Save" else "Create",
            text = initRepeatingDb?.text ?: "",
        )
    )

    fun setText(newText: String) {
        state.update { it.copy(text = newText) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        launchExIo {
            onUi {
                onSuccess()
            }
        }
    }
}
