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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.EventTemplatesVM

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventTemplatesView(
    modifier: Modifier,
    onPick: (EventTemplatesVM.TemplateUI) -> Unit,
) {

    val (_, state) = rememberVM { EventTemplatesVM() }
    val templatesUI = state.templatesUI

    val scrollState = rememberLazyListState()
    LaunchedEffect(templatesUI.firstOrNull()?.text) {
        if (templatesUI.isNotEmpty())
            scrollState.animateScrollToItem(0)
    }

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = H_PADDING_HALF),
        state = scrollState,
    ) {

        itemsIndexed(
            templatesUI,
            key = { _, templateUI -> templateUI.templateDB.id }
        ) { _, templateUI ->

            ListButton(
                text = templateUI.text,
                modifier = Modifier
                    .clip(squircleShape)
                    .animateItemPlacement()
                    .combinedClickable(
                        onClick = {
                            onPick(templateUI)
                        },
                        onLongClick = {
                            Sheet.show { layer ->
                                EventTemplateFormSheet(
                                    layer = layer,
                                    eventTemplate = templateUI.templateDB,
                                )
                            }
                        },
                    ),
            )
        }

        item(key = "add_template") {
            ListButton(
                text = state.newTemplateText,
                modifier = Modifier
                    .clip(squircleShape)
                    .clickable {
                        Sheet.show { layer ->
                            EventTemplateFormSheet(layer = layer, eventTemplate = null)
                        }
                    },
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
        modifier = modifier
            .padding(horizontal = H_PADDING_HALF, vertical = 2.dp),
        color = c.blue,
        fontSize = 14.sp,
    )
}
