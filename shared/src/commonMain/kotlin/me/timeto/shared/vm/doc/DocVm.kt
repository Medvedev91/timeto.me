package me.timeto.shared.vm.doc

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.timeto.shared.db.KvDb
import me.timeto.shared.launchExIo
import me.timeto.shared.time
import me.timeto.shared.vm.Vm

class DocVm : Vm<DocVm.State>() {

    data class State(
        val tmp: Int,
    ) {

        val askQuestionSubject = "Documentation"
    }

    override val state: StateFlow<State> = MutableStateFlow(
        State(tmp = 1)
    )

    fun onRead() {
        launchExIo {
            KvDb.KEY.DOC_FORCE_READ_TIME.upsertInt(time())
        }
    }
}
