@file:OptIn(ExperimentalForeignApi::class)

package me.timeto.shared

import kotlinx.cinterop.*
import kotlinx.coroutines.flow.Flow
import me.timeto.appdbsq.TimetomeDB
import platform.Foundation.*
import platform.UIKit.UIDevice
import me.timeto.shared.db.*

internal actual val REPORT_API_TITLE = " iOS"

fun initKmpIos() {
    val deviceData = DeviceData(
        build = (NSBundle.mainBundle.infoDictionary!!["CFBundleVersion"] as String).toInt(),
        os = "ios-${UIDevice.currentDevice.systemVersion}",
        device = machineIdentifier(),
        flavor = null,
    )
    initKmp(createNativeDriver(DB_NAME, TimetomeDB.Schema), deviceData)
    listenForSyncWatch()
}

actual fun getResourceContent(file: String, type: String): String {
    // Based on https://github.dev/touchlab/DroidconKotlin
    val path = NSBundle.mainBundle.pathForResource(name = file, ofType = type)!!
    return memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = errorPtr.ptr)!!
    }
}

//////

private fun listenForSyncWatch() = launchExDefault {
    initKmpDeferred.join()
    // todo refactor by combine()
    listOf<Flow<*>>(
        ActivityDb.anyChangeFlow(),
        NoteDb.anyChangeFlow(),
        TaskFolderDb.anyChangeFlow(),
        TaskDb.anyChangeFlow(),
        IntervalDb.anyChangeFlow(),
        ChecklistDb.anyChangeFlow(),
        ChecklistItemDb.anyChangeFlow(),
        ShortcutDb.anyChangeFlow(),
    ).forEach { diFlow ->
        var isFirst = true
        diFlow.onEachExIn(this) {
            if (isFirst) {
                isFirst = false
                return@onEachExIn
            }
            IosToWatchSync.syncWatch()
        }
    }
}
