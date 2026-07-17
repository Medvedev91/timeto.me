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
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.unit.dp
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.Screen
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.task_form.TaskFormFs
import me.timeto.app.ui.c
import me.timeto.app.ui.calendar.CalendarTabsView
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.HomeScreen__primaryFontSize
import me.timeto.app.ui.home.bar.HomeBarCalendarButton
import me.timeto.app.ui.home.bar.HomeBarIconButton
import me.timeto.app.ui.home.bar.HomeBarTaskFolderButton
import me.timeto.app.ui.home.bar.homeBarIconSize
import me.timeto.app.ui.home.bar.homeBarLetterSize
import me.timeto.app.ui.notes.NoteFormFs
import me.timeto.app.ui.symbol.SymbolView
import me.timeto.shared.NoteFolderUi
import me.timeto.shared.TaskFolderUi
import me.timeto.shared.vm.home.HomeMode
import me.timeto.shared.vm.home.bar.HomeBarUi
import me.timeto.shared.vm.notes.NoteFormLogic

@Composable
fun HomeTasksBarView(
    homeBarUi: HomeBarUi,
    changeTaskFolder: (TaskFolderUi) -> Unit,
    changeNoteFolder: (NoteFolderUi) -> Unit,
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
                        when (val homeMode = homeBarUi.homeMode) {
                            is HomeMode.TaskFolder -> {
                                navigationFs.push {
                                    TaskFormFs(
                                        strategy = homeMode.addTaskStrategy,
                                    )
                                }
                            }
                            is HomeMode.NoteFolder -> {
                                navigationFs.push {
                                    NoteFormFs(
                                        noteFormLogic = NoteFormLogic.NewNote(
                                            noteFolderDb = homeMode.noteFolderDb,
                                        ),
                                        onDelete = {},
                                    )
                                }
                            }
                        }
                    }
                }
                .padding(start = homeTasksInnerHPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = when (homeBarUi.homeMode) {
                    is HomeMode.TaskFolder -> "Task.."
                    is HomeMode.NoteFolder -> "Note.."
                },
                color = c.secondaryText,
                fontSize = HomeScreen__primaryFontSize,
            )
        }

        homeBarUi.taskFoldersUi.forEach { taskFolderUi ->
            val activeFolderId: Int? =
                (homeBarUi.homeMode as? HomeMode.TaskFolder)?.taskFolderDb?.id
            HomeBarTaskFolderButton(
                taskFolderUi = taskFolderUi,
                color = when {
                    taskFolderUi.taskFolderDb.id != activeFolderId -> c.gray2
                    else -> taskFolderUi.colorRgba.toColor()
                },
                modifier = Modifier,
                onClick = {
                    changeTaskFolder(taskFolderUi)
                },
            )
        }

        homeBarUi.noteFoldersUi.forEach { noteFolderUi ->
            val activeFolderId: Int? =
                (homeBarUi.homeMode as? HomeMode.NoteFolder)?.noteFolderDb?.id
            HomeBarIconButton(
                onClick = {
                    changeNoteFolder(noteFolderUi)
                },
                modifier = Modifier,
            ) {
                SymbolView(
                    symbol = noteFolderUi.symbol,
                    color = when {
                        noteFolderUi.noteFolderDb.id != activeFolderId -> c.gray2
                        else -> c.blue
                    },
                    letterSize = homeBarLetterSize,
                    iconSize = homeBarIconSize,
                    emojiSize = homeBarLetterSize,
                    modifier = Modifier,
                )
            }
        }

        HomeBarCalendarButton(
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
