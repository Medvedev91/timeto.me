package me.timeto.app.ui.home.tasks

import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.unit.dp
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.Screen
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.task_form.TaskFormFs
import me.timeto.shared.vm.home.tasks.HomeTasksBarUi
import me.timeto.app.ui.c
import me.timeto.app.ui.calendar.CalendarTabsView
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.HomeScreen__primaryFontSize
import me.timeto.shared.TaskFolderUi

@Composable
fun HomeTasksBarView(
    tasksBarUi: HomeTasksBarUi,
    changeTaskFolder: (TaskFolderUi) -> Unit,
) {
    val navigationFs = LocalNavigationFs.current

    HStack(
        modifier = Modifier
            .height(HomeScreen__itemHeight)
            .fillMaxWidth()
            .padding(start = homeTasksOuterHPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        HStack(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .padding(end = 8.dp)
                .motionEventSpy { event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        navigationFs.push {
                            TaskFormFs(
                                strategy = tasksBarUi.addTaskStrategy,
                            )
                        }
                    }
                }
                .padding(start = homeTasksInnerHPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = "Task..",
                color = c.secondaryText,
                fontSize = HomeScreen__primaryFontSize,
            )
        }

        tasksBarUi.taskFoldersUi.forEach { taskFolderUi ->
            HomeTasksFolderButton(
                taskFolderUi = taskFolderUi,
                color = when {
                    taskFolderUi.taskFolderDb.id != tasksBarUi.taskFolderDb.id -> c.gray2
                    else -> taskFolderUi.colorRgba.toColor()
                },
                modifier = Modifier,
                onClick = {
                    changeTaskFolder(taskFolderUi)
                },
            )
        }

        HomeTasksCalendarButton(
            color = c.gray2,
            onClick = {
                navigationFs.push {
                    Screen {
                        CalendarTabsView()
                    }
                }
            },
        )
    }
}
