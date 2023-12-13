@file:OptIn(ExperimentalForeignApi::class)

package me.timeto.shared

import kotlinx.cinterop.*
import kotlinx.coroutines.flow.Flow
import me.timeto.appdbsq.TimetomeDB
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*
import platform.UIKit.UIDevice
import platform.darwin.OSStatus
import me.timeto.shared.db.*

internal actual val REPORT_API_TITLE = " iOS"

fun initKmmIos() {
    val deviceData = DeviceData(
        build = (NSBundle.mainBundle.infoDictionary!!["CFBundleVersion"] as String).toInt(),
        os = "ios-${UIDevice.currentDevice.systemVersion}",
        device = machineIdentifier(),
    )
    initKmm(createNativeDriver(DB_NAME, TimetomeDB.Schema), deviceData)
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

// https://github.com/JetBrains/kotlin-native/issues/3013
internal actual object SecureLocalStorage {

    actual fun getOrNull(key: SecureLocalStorage__Key): String? = memScoped {
        val query = buildQuery(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key.buildQueryName(),
            kSecReturnData to kCFBooleanTrue,
        )
        val res = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, res.ptr)
        if (status == errSecItemNotFound)
            return null
        status.assertSuccess("SecureLocalStorage.getOrNull($key)")
        return (CFBridgingRelease(res.value) as NSData).toKotlinString()
    }

    actual fun upsert(key: SecureLocalStorage__Key, value: String?) {
        if (value == null)
            return deleteSafe(key)
        if (getOrNull(key) == null)
            return add(key, value)
        update(key, value)
    }

    private fun add(key: SecureLocalStorage__Key, value: String) {
        val query = buildQuery(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key.buildQueryName(),
            kSecValueData to value.buildQueryString(),
        )
        val status = SecItemAdd(query, null)
        status.assertSuccess("SecureLocalStorage.add($key, $value)")
    }

    private fun update(key: SecureLocalStorage__Key, value: String) {
        val selectQuery = buildQuery(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key.buildQueryName(),
        )
        val updateQuery = buildQuery(
            kSecValueData to value.buildQueryString(),
        )
        val status = SecItemUpdate(selectQuery, updateQuery)
        status.assertSuccess("SecureLocalStorage.update($key, $value)")
    }

    private fun deleteSafe(key: SecureLocalStorage__Key) {
        val query = buildQuery(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to key.buildQueryName(),
        )
        val status = SecItemDelete(query)
        if (status == errSecItemNotFound)
            return
        status.assertSuccess("SecureLocalStorage.deleteSafe($key)")
    }

    // https://stackoverflow.com/q/72244965/5169420
    private fun buildQuery(
        vararg pairs: Pair<CValuesRef<*>?, CValuesRef<*>?>,
    ): CFMutableDictionaryRef = memScoped {
        val dict = CFDictionaryCreateMutable(null, pairs.size.toLong(), null, null)!!
        pairs.forEach { CFDictionaryAddValue(dict, it.first, it.second) }
        return dict
    }

    private fun String.buildQueryString(): CFTypeRef =
        CFBridgingRetain(this.toSwiftData())!!

    private fun SecureLocalStorage__Key.buildQueryName(): CFTypeRef =
        "me.timeto.app-security-storage.$name".buildQueryString()

    @Throws(SecureLocalStorage__Exception::class)
    private fun OSStatus.assertSuccess(errMessage: String) {
        if (this != errSecSuccess)
            throw SecureLocalStorage__Exception("$errMessage\nstatus code:$this")
    }
}

fun NSData.toKotlinString(): String = NSString.create(this, NSUTF8StringEncoding) as String
fun String.toSwiftData(): NSData = (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!

//////

private fun listenForSyncWatch() = launchExDefault {
    initKmmDeferred.join()
    listOf<Flow<*>>(
        ActivityModel.anyChangeFlow(),
        NoteModel.anyChangeFlow(),
        TaskFolderModel.anyChangeFlow(),
        TaskModel.anyChangeFlow(),
        IntervalModel.anyChangeFlow(),
        ChecklistModel.anyChangeFlow(),
        ChecklistItemModel.anyChangeFlow(),
        ShortcutModel.anyChangeFlow(),
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
