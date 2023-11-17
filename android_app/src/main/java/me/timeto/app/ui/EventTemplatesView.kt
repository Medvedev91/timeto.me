package me.timeto.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.EventTemplatesVM

private val listButtonShape = SquircleShape(len = 40f)
private val listButtonPadding = PaddingValues(horizontal = 2.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventTemplatesView(
    spaceAround: Dp,
    paddingTop: Dp,
) {

    val (_, state) = rememberVM { EventTemplatesVM() }
    val templatesUI = state.templatesUI

    val scrollState = rememberLazyListState()
    LaunchedEffect(templatesUI.firstOrNull()?.text) {
        if (templatesUI.isNotEmpty())
            scrollState.animateScrollToItem(0)
    }

    LazyRow(
        modifier = Modifier
            .padding(top = paddingTop),
        contentPadding = PaddingValues(horizontal = spaceAround),
        state = scrollState
    ) {

        itemsIndexed(
            templatesUI,
            key = { _, templateUI -> templateUI.templateDB.id }
        ) { _, templateUI ->

            ListButton(
                text = templateUI.text,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(listButtonShape)
                    .animateItemPlacement()
                    .combinedClickable(
                        onClick = {
                            EventFormSheet__show(
                                editedEvent = null,
                                defText = templateUI.templateDB.text,
                                defTime = templateUI.timeForEventForm,
                            ) {}
                        },
                        onLongClick = {
                            Sheet.show { layer ->
                                EventTemplateFormSheet(
                                    layer = layer,
                                    eventTemplate = templateUI.templateDB,
                                )
                            }
                        },
                    )
                    .padding(listButtonPadding),
            )
        }

        item(key = "add_template") {
            ListButton(
                text = "New Template",
                modifier = Modifier
                    .clip(listButtonShape)
                    .clickable {
                        Sheet.show { layer ->
                            EventTemplateFormSheet(layer = layer, eventTemplate = null)
                        }
                    }
                    .padding(listButtonPadding),
            )
        }
    }
}

@Composable
private fun ListButton(
    text: String,
    modifier: Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = c.blue,
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
    )
}
