package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.db.KVModel
import timeto.shared.db.KVModel.Companion.asFullScreenShowTimeOfTheDay
import timeto.shared.launchExDefault
import timeto.shared.onEachExIn
import timeto.shared.toString10

class FullScreenSettingsSheetVM : __VM<FullScreenSettingsSheetVM.State>() {

    data class State(
        val isShowTimeOfTheDay: Boolean,
    ) {
        val headerTitle = "Full Screen"
        val showTimeOfTheDayTitle = "Show Time of the Day"
    }

    override val state = MutableStateFlow(
        State(
            isShowTimeOfTheDay =
            KVModel.KEY.FULLSCREEN_SHOW_TIME_OF_THE_DAY.getFromDIOrNull().asFullScreenShowTimeOfTheDay()
        )
    )

    fun toggleShowTimeOfTheDay() {
        launchExDefault {
            KVModel.KEY.FULLSCREEN_SHOW_TIME_OF_THE_DAY.upsert(
                (!state.value.isShowTimeOfTheDay).toString10()
            )
        }
    }

    override fun onAppear() {
        val scope = scopeVM()
        KVModel.KEY.FULLSCREEN_SHOW_TIME_OF_THE_DAY.getOrNullFlow().onEachExIn(scope) { kv ->
            state.update { it.copy(isShowTimeOfTheDay = kv?.value.asFullScreenShowTimeOfTheDay()) }
        }
    }
}
