package me.timeto.shared.vm

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import me.timeto.shared.defaultScope

/**
 * WARNING Rules of use due to the specifics of implementation:
 * - Do not launch coroutines inside init() of view models;
 * - Do launch coroutines only in onAppear();
 * - To create coroutines inside life cycle use scopeVM();
 * - onDisappear() does not mean the destroy of view models, on iOS
 *   it can be restarted after onDisappear() by calling onAppear().
 */
abstract class __Vm<T> {

    abstract val state: StateFlow<T>

    ///

    private val scopes = mutableListOf<CoroutineScope>()

    protected fun scopeVm() = defaultScope().apply { scopes.add(this) }

    ///

    open fun onAppear() {}

    fun onDisappear() {
        scopes.forEach { it.cancel() }
        scopes.clear()
    }
}
