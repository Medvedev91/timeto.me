package me.timeto.app.widget

import android.content.Context
import android.content.Intent
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionStartActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.timeto.shared.ioScope
import me.timeto.shared.reportApi

object MyWidgetOpenApp {

    val key = ActionParameters.Key<String>("MyWidget")
    val flow = MutableStateFlow<AppAction?>(null)

    fun emitFlow(action: AppAction?) {
        ioScope().launch {
            flow.emit(action)
        }
    }

    fun buildAction(
        context: Context,
        appAction: AppAction?,
    ): Action {
        val intent: Intent =
            context.packageManager.getLaunchIntentForPackage(context.packageName)!!.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        return actionStartActivity(
            intent = intent,
            parameters = actionParametersOf(key to (appAction?.raw ?: ""))
        )
    }

    sealed class AppAction(
        val raw: String,
    ) {

        companion object {

            fun parse(raw: String): AppAction? = when {
                raw == "new-task" -> NewTask
                raw == "open-calendar" -> OpenCalendar
                raw.startsWith("open-task-folder:") -> OpenTaskFolder(
                    taskFolderId = raw.split(":")[1].toInt(),
                )
                raw.startsWith("open-note-folder:") -> OpenNoteFolder(
                    noteFolderId = raw.split(":")[1].toInt(),
                )
                else -> {
                    reportApi("MyWidgetOpenApp.AppAction.parse(): Invalid App Action $raw")
                    return null
                }
            }
        }

        ///

        object NewTask : AppAction("new-task")

        object OpenCalendar : AppAction("open-calendar")

        data class OpenTaskFolder(
            val taskFolderId: Int,
        ) : AppAction("open-task-folder:$taskFolderId")

        data class OpenNoteFolder(
            val noteFolderId: Int,
        ) : AppAction("open-note-folder:$noteFolderId")
    }
}
