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
        val period: RepeatingDb.Period?,
    ) {

        val textPlaceholder = "Task"

        val periodTitle = "Period"
        val periodNote: String = period?.title ?: "Not Selected"
    }

    override val state = MutableStateFlow(
        State(
            title = if (initRepeatingDb != null) "Edit Repeating" else "New Repeating",
            saveText = if (initRepeatingDb != null) "Save" else "Create",
            text = initRepeatingDb?.text ?: "",
            period = initRepeatingDb?.getPeriod(),
        )
    )

    fun setText(newText: String) {
        state.update { it.copy(text = newText) }
    }

    fun setPeriod(newPeriod: RepeatingDb.Period) {
        state.update { it.copy(period = newPeriod) }
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
