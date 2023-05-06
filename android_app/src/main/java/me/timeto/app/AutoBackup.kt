package me.timeto.app

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import me.timeto.shared.*
import java.util.*
import kotlin.jvm.Throws

@RequiresApi(Build.VERSION_CODES.Q) // Because of MediaStore.MediaColumns.RELATIVE_PATH
class AutoBackup {

    val lastCacheDate = mutableStateOf<Date?>(null)

    init {
        launchExDefault {
            lastCacheDate.value = getLastDate()
        }
    }

    suspend fun dailyBackupIfNeeded() {
        try {
            val lastBackupUnixDay = getLastDate()?.toUnixTime()?.localDay ?: 0
            if (lastBackupUnixDay < UnixTime().localDay) {
                newBackup()
                cleanOld()
            }
        } catch (e: Throwable) {
            zlog(e) // todo report
        }
    }

    @Throws // WARNING
    suspend fun newBackup() {
        val date = Date()
        val jsonBytes = Backup.create("autobackup").toByteArray()

        val dateFormat = "yyyy_MM_dd__HH_mm_ss" // WARNING Логика используется в getLastUnixDay()
        val fileName = "${DateFormat.format(dateFormat, date)}.json"

        ///
        /// May throw IOException.

        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, AUTOBACKUPS_PATH) // RELATIVE_PATH require Build.VERSION_CODES.Q+
        val fileUri = App.instance.contentResolver.insert(getVolume(), values)
            ?: throw Exception("AutoBackup.newBackup() contentResolver.insert() nullable")
        val outputStream = App.instance.contentResolver.openOutputStream(fileUri)
            ?: throw Exception("AutoBackup.newBackup() contentResolver.openOutputStream() nullable")
        outputStream.write(jsonBytes)
        outputStream.close()

        //////

        lastCacheDate.value = date
    }

    ///
    ///

    companion object {

        // # README_APP.md Auto Backup Folder Name
        private const val AUTOBACKUPS_FOLDER_NAME = "timetome_autobackups"
        private const val AUTOBACKUPS_PATH = "Download/$AUTOBACKUPS_FOLDER_NAME"

        @Throws // WARNING
        fun cleanOld() {
            getAutoBackupsSortedDesc()
                .drop(10)
                .forEach { fileData ->
                    // todo log if resCode != 1
                    val resCode = App.instance.contentResolver.delete(
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                        MediaStore.Files.FileColumns._ID + "=?",
                        listOf(fileData.id).toTypedArray(),
                    )
                }
        }

        // # README_APP.md Auto Backup getVolume()
        private fun getVolume() = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

        @Throws // WARNING
        private fun getAutoBackupsSortedDesc(): List<MyFileData> {
            val cursor = App.instance.contentResolver.query(getVolume(), null, null, null, null)
                ?: throw Exception("AutoBackup.getAutoBackupsSortedDesc() cursor nullable")
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

        @Throws // WARNING
        fun getLastDate(): Date? {
            val lastBackupFileName = getAutoBackupsSortedDesc().firstOrNull()?.name ?: return null

            // Should be like 2022_09_07__19_08_39
            val dateArray = lastBackupFileName.split(".").first().replace("__", "_").split("_")
            if (dateArray.size != 6)
                throw Exception("getLastUnixDay() dateArray.count != 6 $lastBackupFileName")

            val dateArrayInts = dateArray.map { it.toIntOrNull() ?: -1 }.toTypedArray()
            if (dateArrayInts.any { it == -1 })
                throw Exception("getLastUnixDay() dateArrayInts $lastBackupFileName")

            val calendar = Calendar.getInstance()
            calendar[Calendar.YEAR] = dateArrayInts[0]
            calendar[Calendar.MONTH] = dateArrayInts[1] - 1
            calendar[Calendar.DAY_OF_MONTH] = dateArrayInts[2]
            calendar[Calendar.HOUR_OF_DAY] = dateArrayInts[3]
            calendar[Calendar.MINUTE] = dateArrayInts[4]
            calendar[Calendar.SECOND] = dateArrayInts[5]
            return calendar.time
        }
    }

    ///
    ///

    private class MyFileData(
        val id: String, // MediaStore.Files.FileColumns._ID
        val name: String, // MediaStore.Files.FileColumns.DISPLAY_NAME
        val path: String, // MediaStore.Files.FileColumns.RELATIVE_PATH
    )
}
