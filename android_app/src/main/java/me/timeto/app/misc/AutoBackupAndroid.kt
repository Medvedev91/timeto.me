package me.timeto.app.misc

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import me.timeto.app.App
import me.timeto.shared.backups.Backup
import me.timeto.shared.UnixTime
import me.timeto.shared.backups.AutoBackup
import me.timeto.shared.reportApi

@RequiresApi(Build.VERSION_CODES.Q) // MediaStore.MediaColumns.RELATIVE_PATH
object AutoBackupAndroid {

    private const val AUTOBACKUPS_FOLDER_NAME = "timetome_autobackups"
    private const val AUTOBACKUPS_PATH = "Download/$AUTOBACKUPS_FOLDER_NAME"

    suspend fun dailyBackupIfNeeded() {
        try {
            val lastBackupUnixDay = getLastTimeOrNull()?.localDay ?: 0
            if (lastBackupUnixDay < UnixTime().localDay) {
                newBackup()
                cleanOld()
            }
        } catch (e: Throwable) {
            reportApi("AutoBackupAndroid.dailyBackupIfNeeded()\n$e")
        }
    }

    @Throws
    suspend fun newBackup() {
        val autoBackupData = AutoBackup.buildAutoBackup()

        //
        // IOException

        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, autoBackupData.fileName)
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, AUTOBACKUPS_PATH) // RELATIVE_PATH require Build.VERSION_CODES.Q+
        val fileUri = App.Companion.instance.contentResolver.insert(getVolume(), values)
                      ?: throw Exception("AutoBackupAndroid.newBackup() contentResolver.insert() nullable")
        val outputStream = App.Companion.instance.contentResolver.openOutputStream(fileUri)
                           ?: throw Exception("AutoBackupAndroid.newBackup() contentResolver.openOutputStream() nullable")
        outputStream.write(autoBackupData.jsonString.toByteArray())
        outputStream.close()

        ///

        AutoBackup.upLastTimeCache(autoBackupData.unixTime)
    }

    @Throws
    fun cleanOld() {
        getAutoBackupsSortedDesc()
            .drop(10)
            .forEach { fileData ->
                // todo log if resCode != 1
                val resCode = App.Companion.instance.contentResolver.delete(
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                    MediaStore.Files.FileColumns._ID + "=?",
                    listOf(fileData.id).toTypedArray(),
                )
            }
    }

    private fun getVolume() = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

    @Throws
    private fun getAutoBackupsSortedDesc(): List<MyFileData> {
        val cursor = App.Companion.instance.contentResolver.query(getVolume(), null, null, null, null)
                     ?: throw Exception("AutoBackupAndroid.getAutoBackupsSortedDesc() cursor nullable")
        val files = mutableListOf<MyFileData>()
        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH))
                // Not checking the full path because it requires checking escaping symbols
                if (!path.contains(AUTOBACKUPS_FOLDER_NAME))
                    continue

                files.add(
                    MyFileData(
                        id = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)),
                        name = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)),
                        path = path
                    )
                )
            }
        }
        return files.sortedByDescending { it.name }
    }

    @Throws
    fun getLastTimeOrNull(): UnixTime? {
        val lastBackup = getAutoBackupsSortedDesc().firstOrNull()?.name ?: return null
        return Backup.fileNameToUnixTime(lastBackup)
    }

    private class MyFileData(
        val id: String, // MediaStore.Files.FileColumns._ID
        val name: String, // MediaStore.Files.FileColumns.DISPLAY_NAME
        val path: String, // MediaStore.Files.FileColumns.RELATIVE_PATH
    )
}