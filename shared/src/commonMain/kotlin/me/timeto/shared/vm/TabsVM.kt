package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.DI
import timeto.shared.db.TaskModel
import timeto.shared.onEachExIn

class TabsVM : __VM<TabsVM.State>() {

    data class State(
        val todayBadge: Int,
    )

    override val state = MutableStateFlow(
        State(todayBadge = DI.tasks.count { it.isToday })
    )

    override fun onAppear() {
        val scope = scopeVM()
        TaskModel.getAscFlow().onEachExIn(scope) { tasks ->
            state.update { it.copy(todayBadge = tasks.count { it.isToday }) }

        }
    }
}
