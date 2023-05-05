package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.DI
import me.timeto.shared.db.TaskModel
import me.timeto.shared.onEachExIn

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
