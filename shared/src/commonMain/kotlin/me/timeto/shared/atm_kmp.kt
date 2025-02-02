package me.timeto.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob

fun zlog(message: Any?): Unit =
    println(";; ${message.toString().replace("\n", "\n;; ")}")

fun ioScope(): CoroutineScope =
    CoroutineScope(SupervisorJob() + Dispatchers.IO)

fun defaultScope() = CoroutineScope(SupervisorJob() + Dispatchers.Default)

//
// Time

expect fun time(): Int
