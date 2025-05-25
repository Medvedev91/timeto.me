package me.timeto.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import me.timeto.shared.vm.__Vm

@Composable
fun <State, VM : __Vm<State>> rememberVm(
    key1: Any? = null,
    key2: Any? = null,
    key3: Any? = null,
    block: () -> VM,
): Pair<VM, State> {
    val vm = remember(key1, key2, key3) {
        block()
    }
    DisposableEffect(key1, key2, key3) {
        onDispose {
            vm.onDisappear()
        }
    }
    return vm to vm.state.collectAsState().value
}
