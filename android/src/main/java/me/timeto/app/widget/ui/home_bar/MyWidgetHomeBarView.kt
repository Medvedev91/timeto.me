package me.timeto.app.widget.ui.home_bar

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import me.timeto.app.toColor
import me.timeto.app.toGlanceColorProvider
import me.timeto.app.ui.c
import me.timeto.app.widget.MyWidgetOpenApp
import me.timeto.app.widget.ui.myWidgetHPadding
import me.timeto.app.widget.ui.myWidgetItemHeight
import me.timeto.app.widget.ui.myWidgetPrimaryFontSize
import me.timeto.shared.vm.home.HomeMode
import me.timeto.shared.vm.home.bar.HomeBarUi

@Composable
fun MyWidgetHomeBarView(
    homeBarUi: HomeBarUi,
) {
    val context: Context = LocalContext.current

    Row(
        modifier = GlanceModifier
            .height(myWidgetItemHeight)
            .fillMaxWidth()
            .padding(start = myWidgetHPadding, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Row(
            modifier = GlanceModifier
                .fillMaxHeight()
                .defaultWeight()
                .padding(end = 8.dp)
                .clickable(
                    MyWidgetOpenApp.buildAction(
                        context = context,
                        appAction = MyWidgetOpenApp.AppAction.NewTask,
                    ),
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = when (homeBarUi.homeMode) {
                    is HomeMode.TaskFolder -> "Task.."
                    is HomeMode.NoteFolder -> "Note.."
                },
                style = TextStyle(
                    color = c.secondaryText.toGlanceColorProvider(),
                    fontSize = myWidgetPrimaryFontSize,
                ),
            )
        }

        homeBarUi.taskFoldersUi.forEach { taskFolderUi ->
            val activeFolderId: Int? =
                (homeBarUi.homeMode as? HomeMode.TaskFolder)?.taskFolderDb?.id
            MyWidgetHomeBarTaskFolderButton(
                taskFolderUi = taskFolderUi,
                color = when {
                    taskFolderUi.taskFolderDb.id != activeFolderId -> c.gray2
                    else -> taskFolderUi.colorRgba.toColor()
                },
                glanceModifier = GlanceModifier,
                onClickAction = MyWidgetOpenApp.buildAction(
                    context = context,
                    appAction = MyWidgetOpenApp.AppAction.OpenTaskFolder(
                        taskFolderId = taskFolderUi.taskFolderDb.id,
                    ),
                ),
            )
        }

        homeBarUi.noteFoldersUi.forEach { noteFolderUi ->
            val activeFolderId: Int? =
                (homeBarUi.homeMode as? HomeMode.NoteFolder)?.noteFolderDb?.id
            MyWidgetHomeBarNoteFolderButton(
                noteFolderUi = noteFolderUi,
                color = when {
                    noteFolderUi.noteFolderDb.id != activeFolderId -> c.gray2
                    else -> c.blue
                },
                onClickAction = MyWidgetOpenApp.buildAction(
                    context = context,
                    appAction = MyWidgetOpenApp.AppAction.OpenNoteFolder(
                        noteFolderId = noteFolderUi.noteFolderDb.id,
                    ),
                ),
            )
        }

        MyWidgetHomeBarCalendarButton(
            color = c.gray2,
            onClickAction = MyWidgetOpenApp.buildAction(
                context = context,
                appAction = MyWidgetOpenApp.AppAction.OpenCalendar,
            ),
        )
    }
}
