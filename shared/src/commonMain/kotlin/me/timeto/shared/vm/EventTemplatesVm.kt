package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.EventTemplateDb

class EventTemplatesVm : __Vm<EventTemplatesVm.State>() {

    data class TemplateUI(
        val templateDB: EventTemplateDb,
        val text: String,
    ) {
        val timeForEventForm: Int = UnixTime().localDayStartTime() + templateDB.daytime
    }

    data class State(
        val templatesUI: List<TemplateUI>
    ) {
        val newTemplateText = "New Template"
    }

    override val state = MutableStateFlow(
        State(
            templatesUI = Cache.eventTemplatesDbSorted.toTemplatesUI(),
        )
    )

    override fun onAppear() {
        val scope = scopeVm()
        EventTemplateDb.selectAscSortedFlow().onEachExIn(scope) { templatesDB ->
            state.update {
                it.copy(templatesUI = templatesDB.toTemplatesUI())
            }
        }
    }
}

private fun List<EventTemplateDb>.toTemplatesUI() = this
    .reversed()
    .map { templateDB ->
        EventTemplatesVm.TemplateUI(
            templateDB = templateDB,
            text = templateDB.text.textFeatures().textNoFeatures.let {
                if (it.length <= 17) it else it.substring(0..14) + ".."
            },
        )
    }
