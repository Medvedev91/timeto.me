package me.timeto.shared.vm

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import me.timeto.shared.misc.ioScope

abstract class __Vm<T> {

    abstract val state: StateFlow<T>

    ///

    private val scopes = mutableListOf<CoroutineScope>()

    protected fun scopeVm(): CoroutineScope =
        ioScope().apply { scopes.add(this) }

    ///

    suspend fun onUi(block: () -> Unit) {
        withContext(Dispatchers.Main) { block() }
    }

    fun onDisappear() {
        scopes.forEach { it.cancel() }
        scopes.clear()
    }
}
