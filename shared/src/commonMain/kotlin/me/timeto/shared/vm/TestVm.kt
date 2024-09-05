package me.timeto.shared.vm

import kotlinx.coroutines.flow.*

class TestVm : __Vm<TestVm.State>() {

    data class State(
        val count: Int,
    )

    override val state = MutableStateFlow(
        State(
            count = 0
        )
    )

    fun inc() {
        state.update { it.copy(count = it.count + 1) }
    }

    override fun onAppear() {
    }
}
