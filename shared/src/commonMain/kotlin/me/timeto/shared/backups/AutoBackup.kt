package me.timeto.shared.backups

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.UnixTime

object AutoBackup {

    val lastTimeCache = MutableStateFlow<UnixTime?>(null)

    fun upLastTimeCache(unixTime: UnixTime?) {
        lastTimeCache.update { unixTime }
    }

    suspend fun buildAutoBackup(): AutoBackupData {
        val unixTime = UnixTime()
        return AutoBackupData(
            unixTime = unixTime,
            jsonString = Backup.create("autobackup"),
            fileName = Backup.prepFileName(unixTime, prefix = "")
        )
    }

    class AutoBackupData(
        val unixTime: UnixTime,
        val jsonString: String,
        val fileName: String,
    )
}
