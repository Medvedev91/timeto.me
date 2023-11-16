package me.timeto.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.EventTemplatesVM

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventTemplatesView(
    spaceAround: Dp,
    paddingTop: Dp,
) {

    val (vm, state) = rememberVM { EventTemplatesVM() }
    val templatesUI = state.templatesUI

    val scrollState = rememberLazyListState()
    LaunchedEffect(templatesUI.firstOrNull()?.text) {
        if (templatesUI.isNotEmpty())
            scrollState.animateScrollToItem(0)
    }

    LazyRow(
        modifier = Modifier
            .padding(top = if (templatesUI.isEmpty()) 0.dp else paddingTop),
        contentPadding = PaddingValues(horizontal = spaceAround),
        state = scrollState
    ) {

        itemsIndexed(
            templatesUI,
            key = { _, templateUI -> templateUI.templateDB.id }
        ) { _, templateUI ->

            Text(
                templateUI.text,
                modifier = Modifier
                    .padding(end = if (templateUI == templatesUI.last()) 0.dp else 8.dp)
                    .clip(roundedShape)
                    .background(c.blue)
                    .padding(1.dp)
                    .animateItemPlacement()
                    .combinedClickable(
                        onClick = {
                            EventFormSheet__show(
                                editedEvent = null,
                                defText = templateUI.templateDB.text,
                                defTime = templateUI.templateDB.daytime,
                            ) {}
                        },
                        onLongClick = {
                            vm.delTemplate(templateUI)
                            vibrateShort()
                        },
                    )
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 5.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.W600,
                color = c.white,
            )
        }
    }
}
