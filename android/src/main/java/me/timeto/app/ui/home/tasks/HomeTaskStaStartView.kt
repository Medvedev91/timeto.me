package me.timeto.app.ui.home.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.timeto.app.R
import me.timeto.app.ui.HStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.events.EventFormFs
import me.timeto.app.ui.home.HomeScreen__itemCircleHeight
import me.timeto.app.ui.home.bar.HomeBarCalendarButton
import me.timeto.app.ui.home.bar.HomeBarTaskFolderButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.task_form.TaskFormFs
import me.timeto.shared.vm.home.tasks.HomeTasksItemUi

// STA - Swipe to Action
@Composable
fun HomeTaskStaStartView(
    homeTaskUi: HomeTasksItemUi.HomeTaskUi,
    resetSta: (() -> Unit) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val navigationFs = LocalNavigationFs.current

    HStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.blue),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        ZStack(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(HomeScreen__itemCircleHeight)
                .clip(roundedShape)
                .background(c.white)
                .clickable {
                    resetSta({})
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(id = R.drawable.sf_xmark_small_medium),
                contentDescription = "Close",
                tint = c.blue,
                modifier = Modifier
                    .size(10.dp),
            )
        }

        ZStack(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 8.dp)
                .clip(roundedShape)
                .clickable {
                    scope.launch {
                        delay(200L)
                        resetSta({})
                    }
                    navigationFs.push {
                        TaskFormFs(strategy = homeTaskUi.editStrategy)
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {

            Text(
                text = "Edit..",
                modifier = Modifier
                    .padding(start = 8.dp),
                color = c.white,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        homeTaskUi.staTaskFoldersUi.forEach { staFolderUi ->
            HomeBarTaskFolderButton(
                taskFolderUi = staFolderUi.taskFolderUi,
                color = if (staFolderUi.isSelected) c.white else c.secondaryText,
                modifier = Modifier,
                onClick = {
                    resetSta {
                        staFolderUi.onTap()
                    }
                },
            )
        }

        HomeBarCalendarButton(
            color = c.secondaryText,
            onClick = {
                scope.launch {
                    delay(200L)
                    resetSta({})
                }
                navigationFs.push {
                    EventFormFs(
                        initEventDb = null,
                        initText = homeTaskUi.taskUi.taskDb.text,
                        initTime = null,
                        onDone = {
                            homeTaskUi.taskUi.delete()
                        },
                    )
                }
            },
        )
    }
}
