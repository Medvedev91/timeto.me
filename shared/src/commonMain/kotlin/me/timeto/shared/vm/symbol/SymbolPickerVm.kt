package me.timeto.shared.vm.symbol

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.Symbol
import me.timeto.shared.vm.Vm

class SymbolPickerVm : Vm<SymbolPickerVm.State>() {

    data class State(
        val symbolChunks: List<List<Symbol>>,
    )

    override val state = MutableStateFlow(
        State(
            symbolChunks = Symbol.Icon.IconEnum.entries
                .map { it.toIcon() }
                .chunked(8),
        )
    )
}
