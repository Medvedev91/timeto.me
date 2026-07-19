package me.timeto.app.ui.home.bar

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import me.timeto.app.R
import me.timeto.app.ui.symbol.SymbolView
import me.timeto.shared.TaskFolderUi
import me.timeto.shared.vm.home.bar.HomeBarAnimate

@Composable
fun HomeBarTaskFolderButton(
    taskFolderUi: TaskFolderUi,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {

    val scaleAnimation = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        HomeBarAnimate.flow.collect { homeBarAnimate ->
            if ((homeBarAnimate is HomeBarAnimate.TaskFolder) &&
                (homeBarAnimate.taskFolderId == taskFolderUi.taskFolderDb.id)
            ) {
                homeBarButtonAnimate(scaleAnimation)
            }
        }
    }

    HomeBarIconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        if (taskFolderUi.taskFolderDb.isToday) {
            Icon(
                painterResource(id = R.drawable.ms_wb_sunny_fill),
                contentDescription = "Today",
                tint = color,
                modifier = Modifier
                    .size(homeBarIconSize)
                    .scale(scaleAnimation.value),
            )
        } else if (taskFolderUi.taskFolderDb.isTomorrow) {
            Icon(
                painterResource(id = R.drawable.ms_dark_mode_fill),
                contentDescription = "Tomorrow",
                tint = color,
                modifier = Modifier
                    .size(homeBarIconSize)
                    .scale(scaleAnimation.value),
            )
        } else {
            SymbolView(
                symbol = taskFolderUi.symbol,
                color = color,
                letterSize = homeBarLetterSize,
                iconSize = homeBarIconSize,
                emojiSize = homeBarLetterSize,
                modifier = Modifier
                    .scale(scaleAnimation.value),
            )
        }
    }
}
