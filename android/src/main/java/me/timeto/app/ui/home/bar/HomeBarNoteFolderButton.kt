package me.timeto.app.ui.home.bar

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import me.timeto.app.ui.symbol.SymbolView
import me.timeto.shared.NoteFolderUi
import me.timeto.shared.vm.home.bar.HomeBarAnimate

@Composable
fun HomeBarNoteFolderButton(
    noteFolderUi: NoteFolderUi,
    color: Color,
    onClick: () -> Unit,
) {

    val scaleAnimation = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        HomeBarAnimate.flow.collect { homeBarAnimate ->
            if ((homeBarAnimate is HomeBarAnimate.NoteFolder) &&
                (homeBarAnimate.noteFolderId == noteFolderUi.noteFolderDb.id)
            ) {
                homeBarButtonAnimate(scaleAnimation)
            }
        }
    }

    HomeBarIconButton(
        onClick = onClick,
        modifier = Modifier,
    ) {
        SymbolView(
            symbol = noteFolderUi.symbol,
            color = color,
            letterSize = homeBarLetterSize,
            iconSize = homeBarIconSize,
            emojiSize = homeBarLetterSize,
            modifier = Modifier
                .scale(scaleAnimation.value),
        )
    }
}
