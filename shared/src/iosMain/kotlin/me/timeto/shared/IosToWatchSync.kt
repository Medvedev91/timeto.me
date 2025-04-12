@file:OptIn(ExperimentalForeignApi::class)

package me.timeto.shared

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.*
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import platform.WatchConnectivity.WCSession
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.misc.timeMls
import me.timeto.shared.models.TimerDataUi

object IosToWatchSync {

    /**
     * updateApplicationContext() does not re-send the same data,
     * this requires a backup of the same format. But this is NOT used,
     * because each request sends a unique type. Documentation below.
     */
    fun syncWatch(): Unit = launchExDefault {

        if (!WCSession.isSupported())
            return@launchExDefault
        val session = WCSession.defaultSession
        if (!session.isPaired())
            return@launchExDefault
        if (!session.isWatchAppInstalled())
            return@launchExDefault

        /**
         * If data is sent to the watch several times in a short time, it may be received
         * in reverse order. That is, the oldest data will be the last to arrive. That is
         * why time in milliseconds is sent to type, the clock is checked: synchronization
         * requests with type less than it was before are ignored.
         */
        val type = "${timeMls()}"

        val jString = Backup.create(type, intervalsLimit = 1)
        // todo error?
        session.updateApplicationContext(mapOf("backup" to jString), error = null)
    }

    /**
     * All operations have to be done in a single transaction, otherwise
     * several states will be sent practically at the same moment, this
     * causes the UI watch to twitch. Sometimes the intermediate state comes
     * after the final state, resulting in irrelevant data on the watch.
     */
    fun didReceiveMessageData(
        jString: String,
        onFinish: (String) -> Unit,
    ): Unit = launchExDefault {

        val jRequest = Json.parseToJsonElement(jString).jsonObject
        val command = jRequest["command"]!!.jsonPrimitive.content
        val jData = jRequest["data"]!!.jsonObject

        if (command == "start_interval") {
            val timer = jData["timer"]!!.jsonPrimitive.int
            val activity = ActivityDb.selectByIdOrNull(jData["activity_id"]!!.jsonPrimitive.int)!!
            val note = jData["note"]?.jsonPrimitive?.contentOrNull
            IntervalDb.insertWithValidation(
                timer = timer,
                activity = activity,
                note = note,
            )
            onFinish("{}")
            return@launchExDefault
        }

        if (command == "start_task") {
            val timer = jData["timer"]!!.jsonPrimitive.int
            val activity = ActivityDb.selectByIdOrNull(jData["activity_id"]!!.jsonPrimitive.int)!!
            val task = TaskDb.getByIdOrNull(jData["task_id"]!!.jsonPrimitive.int)!!
            task.startInterval(
                timer = timer,
                activity = activity,
            )
            onFinish("{}")
            return@launchExDefault
        }

        if (command == "toggle_pomodoro") {
            TimerDataUi(
                interval = IntervalDb.selectLastOneOrNull()!!,
                todayTasks = Cache.tasksDb.filter { it.isToday },
                isPurple = false,
            ).togglePomodoro()
            onFinish("{}")
            return@launchExDefault
        }

        if (command == "sync") {
            syncWatch()
            onFinish("{}")
            return@launchExDefault
        }

        reportApi("No command for IosToWatchSync.didReceiveMessageData()\n$jString")
    }
}
