package me.timeto.app.ui.home.tasks

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import me.timeto.app.R
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.symbol.SymbolView
import me.timeto.shared.TaskFolderUi
import me.timeto.shared.vm.home.tasks.homeTasksBarFolderAnimateFlow

@Composable
fun HomeTasksFolderButton(
    taskFolderUi: TaskFolderUi,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val scaleAnimation = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        homeTasksBarFolderAnimateFlow.collect { folderId ->
            if (folderId == taskFolderUi.taskFolderDb.id) {
                scaleAnimation.animateTo(1.40f)
                scaleAnimation.animateTo(1f)
                scaleAnimation.animateTo(1.25f)
                scaleAnimation.animateTo(1f)
            }
        }
    }

    ZStack(
        modifier = modifier
            .size(HomeScreen__itemHeight)
            .clip(roundedShape)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        if (taskFolderUi.taskFolderDb.isToday) {
            Icon(
                painterResource(id = R.drawable.ms_wb_sunny_fill),
                contentDescription = "Today",
                tint = color,
                modifier = Modifier
                    .size(homeTasksBarIconSize)
                    .scale(scaleAnimation.value),
            )
        } else if (taskFolderUi.taskFolderDb.isTomorrow) {
            Icon(
                painterResource(id = R.drawable.ms_dark_mode_fill),
                contentDescription = "Tomorrow",
                tint = color,
                modifier = Modifier
                    .size(homeTasksBarIconSize)
                    .scale(scaleAnimation.value),
            )
        } else {
            SymbolView(
                symbol = taskFolderUi.symbol,
                color = color,
                letterSize = homeTasksBarLetterSize,
                iconSize = homeTasksBarIconSize,
                emojiSize = homeTasksBarLetterSize,
                modifier = Modifier
                    .scale(scaleAnimation.value),
            )
        }
    }
}
