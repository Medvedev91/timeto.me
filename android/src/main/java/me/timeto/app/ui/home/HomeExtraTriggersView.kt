package me.timeto.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.c
import me.timeto.app.ui.squircleShape
import me.timeto.app.ui.checklists.ChecklistScreen
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.shared.vm.home.HomeVm
import me.timeto.shared.performUi

@Composable
fun HomeExtraTriggersView(
    extraTriggers: HomeVm.ExtraTriggers,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {

    if (extraTriggers.checklistsDb.isEmpty() &&
        extraTriggers.shortcutsDb.isEmpty()
    ) {
        return
    }

    val navigationFs = LocalNavigationFs.current

    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {

        extraTriggers.shortcutsDb.forEach { shortcutDb ->
            item {
                ButtonView(
                    title = shortcutDb.name,
                    iconType = IconType.shortcut,
                    onClick = {
                        shortcutDb.performUi()
                    },
                )
            }
        }

        extraTriggers.checklistsDb.forEach { checklistDb ->
            item {
                ButtonView(
                    title = checklistDb.name,
                    iconType = IconType.checklist,
                    onClick = {
                        navigationFs.push {
                            ChecklistScreen(
                                checklistDb = checklistDb,
                                withNavigationPadding = true,
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ButtonView(
    title: String,
    iconType: IconType,
    onClick: () -> Unit,
) {

    val color: Color = when (iconType) {
        IconType.checklist -> c.green
        IconType.shortcut -> c.red
    }

    HStack(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .height(26.dp)
            .clip(squircleShape)
            .background(color)
            .clickable {
                onClick()
            }
            .padding(start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = FontWeight.W400,
            color = c.white,
        )
    }
}

private enum class IconType {
    checklist,
    shortcut,
}
