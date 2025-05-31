package me.timeto.shared.vm

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import me.timeto.shared.ioScope

abstract class __Vm<T> {

    abstract val state: StateFlow<T>

    private val scopes = mutableListOf<CoroutineScope>()
    private var isDestroyed: Boolean = false

    protected fun scopeVm(): CoroutineScope =
        ioScope().apply { scopes.add(this) }

    suspend fun onUi(block: () -> Unit) {
        if (isDestroyed)
            return
        withContext(Dispatchers.Main) { block() }
    }

    fun onDestroy() {
        isDestroyed = true
        scopes.forEach { it.cancel() }
        scopes.clear()
    }
}
