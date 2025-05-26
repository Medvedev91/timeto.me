package me.timeto.shared

import kotlinx.cinterop.UnsafeNumber
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.WatchConnectivity.WCSession
import me.timeto.shared.db.*
import me.timeto.shared.misc.backups.Backupable__Holder
import me.timeto.shared.misc.backups.Backupable__Item
import me.timeto.shared.misc.zlog

/**
 * I use application context for backup because of limits:
 * https://stackoverflow.com/a/35076706/5169420
 * I mean the option - send a request and immediately get a backup
 * does not work, but it is ok, because for responsive UI data are
 * updated locally on the watch and only then synchronized with iPhone.
 *
 * While using the app, I ran into a limit of 65.5 KB. This is mainly
 * for the history of the interval for hints. 262.1 KB should be enough,
 * we can look for optimization.
 */
object WatchToIosSync {

    private const val LOCAL_DELAY_MLS = 300L

    fun sync() {
        requestFromAppleWatch(
            command = "sync",
            jData = JsonObject(mapOf()),
        )
    }

    fun startIntervalWithLocal(
        activity: ActivityDb,
        seconds: Int,
    ): Unit = launchExIo {
        val interval = activity.startInterval(seconds)
        launchEx {
            delay(LOCAL_DELAY_MLS)
            val map = mapOf(
                "activity_id" to JsonPrimitive(activity.id),
                "timer" to JsonPrimitive(seconds),
                "note" to JsonPrimitive(interval.note),
            )
            requestFromAppleWatch(
                command = "start_interval",
                jData = JsonObject(map)
            )
        }
    }

    fun startTaskWithLocal(
        activity: ActivityDb,
        timer: Int,
        task: TaskDb,
    ): Unit = launchExIo {
        task.startInterval(timer, activity)
        launchEx {
            delay(LOCAL_DELAY_MLS)
            val map = mapOf(
                "activity_id" to JsonPrimitive(activity.id),
                "timer" to JsonPrimitive(timer),
                "task_id" to JsonPrimitive(task.id),
            )
            requestFromAppleWatch(
                command = "start_task",
                jData = JsonObject(map)
            )
        }
    }

    fun togglePomodoro() {
        launchExIo {
            val map = mapOf<String, JsonPrimitive>()
            requestFromAppleWatch(
                command = "toggle_pomodoro",
                jData = JsonObject(map)
            )
        }
    }

    ///
    /// Smart Restore

    private var lastSyncId: Long? = null

    fun smartRestore(
        jsonString: String,
    ): Unit = launchExIo {
        // Transaction to avoid many UI updates
        db.transaction {
            val json = Json.parseToJsonElement(jsonString)

            val newSyncId = json.jsonObject["type"]!!.jsonPrimitive.long
            val lastSyncIdLocal = lastSyncId
            if (lastSyncIdLocal != null && lastSyncIdLocal > newSyncId)
                return@transaction
            lastSyncId = newSyncId

            // Ordering is important
            // WARNING The same models must be in listenForSyncWatch()
            val checklistItems = smartRestore__start(ChecklistItemDb, json.jsonObject["checklist_items"]!!.jsonArray)
            val checklists = smartRestore__start(ChecklistDb, json.jsonObject["checklists"]!!.jsonArray)
            val shortcuts = smartRestore__start(ShortcutDb, json.jsonObject["shortcuts"]!!.jsonArray)
            val intervals = smartRestore__start(
                IntervalDb,
                json.jsonObject["intervals"]!!.jsonArray,
                doNotUpdate = true,
            )
            val tasks = smartRestore__start(TaskDb, json.jsonObject["tasks"]!!.jsonArray)
            val taskFolders = smartRestore__start(TaskFolderDb, json.jsonObject["task_folders"]!!.jsonArray)
            val activities = smartRestore__start(ActivityDb, json.jsonObject["activities"]!!.jsonArray)

            activities()
            taskFolders()
            tasks()
            intervals()
            shortcuts()
            checklists()
            checklistItems()

            // To 100% ensure
            val ifl = IntervalDb.selectFirstAndLastNeedTransaction()
            Cache.fillLateInit(firstInterval = ifl.first, lastInterval = ifl.second)
        }
    }
}

private fun smartRestore__start(
    backupableHolder: Backupable__Holder,
    jArray: JsonArray,
    doNotUpdate: Boolean = false,
): (() -> Unit) {
    val newIds = jArray.map { it.jsonArray[0].jsonPrimitive.content }.toSet()
    backupableHolder.backupable__getAll().forEach { item ->
        if (!newIds.contains(item.backupable__getId()))
            item.backupable__delete()
    }
    return {
        val oldItemsMap: Map<String, Backupable__Item> = backupableHolder
            .backupable__getAll()
            .associateBy { it.backupable__getId() }

        jArray
            .map { it.jsonArray }
            .forEach { j ->
                val newId = j[0].jsonPrimitive.content
                val oldItem = oldItemsMap[newId]
                if (oldItem != null) {
                    if (!doNotUpdate && (oldItem.backupable__backup().toString() != j.toString()))
                        oldItem.backupable__update(j)
                    return@forEach
                }
                backupableHolder.backupable__restore(j)
            }
    }
}

@OptIn(UnsafeNumber::class)
private fun requestFromAppleWatch(
    command: String,
    jData: JsonElement,
    errDelayMls: Long = 5_000L,
    onResponse: ((String) -> Unit)? = null,
) {
    if (!WCSession.isSupported())
        return

    val jRequest = JsonObject(
        mapOf(
            "command" to JsonPrimitive(command),
            "data" to jData,
        )
    )
    val requestString = jRequest.toString() as NSString
    val requestData = requestString.dataUsingEncoding(NSUTF8StringEncoding)
    if (requestData == null) {
        reportApi("requestFromAppleWatch() REQUEST data is null\n$requestString")
        return
    }

    var isResponseReceived = false
    WCSession.defaultSession.sendMessageData(
        requestData,
        replyHandler = { responseData ->
            isResponseReceived = true

            if (responseData == null) {
                reportApi("requestFromAppleWatch() RESPONSE data is null\n$command\n$requestString")
                return@sendMessageData
            }

            onResponse?.invoke(NSString.create(responseData, NSUTF8StringEncoding) as String)
        },
        errorHandler = { error ->
            launchExIo {
                reportApi("requestFromAppleWatch() errorHandler:\n${error?.localizedDescription}")
                showUiAlert(error?.localizedDescription ?: "Internal Error")
            }
        }
    )
    launchExIo {
        delay(errDelayMls)
        if (!isResponseReceived) {
            zlog("Sync Error")
            // showUiAlert("Sync Error") // todo
        }
    }
}
