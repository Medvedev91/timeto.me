package me.timeto.shared.ui.events.templates

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.EventTemplateDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.ui.__Vm

class EventTemplatesVm : __Vm<EventTemplatesVm.State>() {

    data class State(
        val templatesUi: List<EventTemplateUi>
    ) {
        val newTemplateText = "New Template"
    }

    override val state = MutableStateFlow(
        State(
            templatesUi = Cache.eventTemplatesDbSorted.toTemplatesUi(),
        )
    )

    init {
        val scope = scopeVm()
        EventTemplateDb.selectAscSortedFlow().onEachExIn(scope) { templatesDb ->
            state.update {
                it.copy(templatesUi = templatesDb.toTemplatesUi())
            }
        }
    }
}

private fun List<EventTemplateDb>.toTemplatesUi(
): List<EventTemplateUi> = this.reversed().map { eventTemplateDb ->
    EventTemplateUi(
        eventTemplateDb = eventTemplateDb,
    )
}
