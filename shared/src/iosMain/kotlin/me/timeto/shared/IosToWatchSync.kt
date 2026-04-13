@file:OptIn(ExperimentalForeignApi::class)

package me.timeto.shared

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.*
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import platform.WatchConnectivity.WCSession
import me.timeto.shared.backups.Backup
import me.timeto.shared.db.ActivityDb

object IosToWatchSync {

    /**
     * updateApplicationContext() does not re-send the same data,
     * this requires a backup of the same format. But this is NOT used,
     * because each request sends a unique type. Documentation below.
     */
    fun syncWatch(): Unit = launchExIo {

        if (!WCSession.isSupported())
            return@launchExIo
        val session = WCSession.defaultSession
        if (!session.isPaired())
            return@launchExIo
        if (!session.isWatchAppInstalled())
            return@launchExIo

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
    ): Unit = launchExIo {

        val jRequest = Json.parseToJsonElement(jString).jsonObject
        val command = jRequest["command"]!!.jsonPrimitive.content
        val jData = jRequest["data"]!!.jsonObject

        if (command == "start_interval") {
            val activityDb: ActivityDb =
                ActivityDb.selectByIdOrNull(jData["activity_id"]!!.jsonPrimitive.int)!!
            val timer: Int = jData["timer"]!!.jsonPrimitive.intOrNull ?: run {
                DayBarsUi.buildToday().buildActivityStats(activityDb).calcRestOfGoal()
            }
            val note = (jData["note"]?.jsonPrimitive?.contentOrNull ?: "").textFeatures().copy(
                timerType = TextFeatures.TimerType.Timer(timer)
            ).textWithFeatures()
            activityDb.startInterval(note = note)
            onFinish("{}")
            return@launchExIo
        }

        if (command == "start_task") {
            val activityDb: ActivityDb =
                ActivityDb.selectByIdOrNull(jData["activity_id"]!!.jsonPrimitive.int)!!
            val taskDb: TaskDb =
                TaskDb.selectByIdOrNull(jData["task_id"]!!.jsonPrimitive.int)!!
            val timer: Int = jData["timer"]!!.jsonPrimitive.intOrNull ?: run {
                DayBarsUi.buildToday().buildActivityStats(activityDb).calcRestOfGoal()
            }
            taskDb.startTimer(
                seconds = timer,
                activityDb = activityDb,
            )
            onFinish("{}")
            return@launchExIo
        }

        if (command == "toggle_pomodoro") {
            TimerStateUi(
                intervalDb = IntervalDb.selectLastOneOrNull()!!,
                todayTasksDb = Cache.tasksDb.filter { it.isToday },
                isPurple = false,
            ).togglePomodoro()
            onFinish("{}")
            return@launchExIo
        }

        if (command == "sync") {
            syncWatch()
            onFinish("{}")
            return@launchExIo
        }

        reportApi("No command for IosToWatchSync.didReceiveMessageData()\n$jString")
    }
}
