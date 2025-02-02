package me.timeto.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob

fun ioScope(): CoroutineScope =
    CoroutineScope(SupervisorJob() + Dispatchers.IO)
