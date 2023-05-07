package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

object AutoBackup {

    val lastTimeCache = MutableStateFlow<UnixTime?>(null)

    fun upLastTimeCache(unixTime: UnixTime?) {
        lastTimeCache.update { unixTime }
    }
}
