@file:OptIn(ExperimentalForeignApi::class)

package me.timeto.shared

import kotlinx.cinterop.*
import kotlinx.coroutines.flow.launchIn
import me.timeto.appdbsq.TimetomeDB
import platform.Foundation.*
import platform.UIKit.UIDevice
import me.timeto.shared.db.*
import me.timeto.shared.misc.SystemInfo
import me.timeto.shared.misc.combine

fun initKmpIos() {
    val systemInfo = SystemInfo(
        build = (NSBundle.mainBundle.infoDictionary!!["CFBundleVersion"] as String).toInt(),
        version = NSBundle.mainBundle.infoDictionary!!["CFBundleShortVersionString"] as String,
        os = SystemInfo.Os.Ios(UIDevice.currentDevice.systemVersion),
        device = machineIdentifier(),
        flavor = null,
    )
    initKmp(createNativeDriver(DB_NAME, TimetomeDB.Schema), systemInfo)
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

private fun listenForSyncWatch() = launchExIo {
    initKmpDeferred.join()
    combine(
        ActivityDb.anyChangeFlow(),
        NoteDb.anyChangeFlow(),
        TaskFolderDb.anyChangeFlow(),
        TaskDb.anyChangeFlow(),
        IntervalDb.anyChangeFlow(),
        ChecklistDb.anyChangeFlow(),
        ChecklistItemDb.anyChangeFlow(),
        ShortcutDb.anyChangeFlow(),
    ) { _, _, _, _, _, _, _, _ ->
        IosToWatchSync.syncWatch()
    }.launchIn(this)
}
