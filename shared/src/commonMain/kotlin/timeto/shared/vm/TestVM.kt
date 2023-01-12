package timeto.shared.vm

import kotlinx.coroutines.flow.*

class TestVM : __VM<TestVM.State>() {

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
