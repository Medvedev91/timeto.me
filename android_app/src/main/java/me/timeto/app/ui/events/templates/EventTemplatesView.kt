package me.timeto.app.ui.events.templates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.c
import me.timeto.app.ui.rememberVm
import me.timeto.app.squircleShape
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.shared.ui.events.templates.EventTemplateUi
import me.timeto.shared.ui.events.templates.EventTemplatesVm

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventTemplatesView(
    modifier: Modifier,
    onDone: (EventTemplateUi) -> Unit,
) {

    val navigationFs = LocalNavigationFs.current

    val (_, state) = rememberVm {
        EventTemplatesVm()
    }

    val templatesUi = state.templatesUi

    val scrollState = rememberLazyListState()
    LaunchedEffect(templatesUi.firstOrNull()?.shortText) {
        if (templatesUi.isNotEmpty())
            scrollState.animateScrollToItem(0)
    }

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = H_PADDING_HALF),
        state = scrollState,
    ) {

        templatesUi.forEach { templateUi ->

            item(key = templateUi.eventTemplateDb.id) {
                ListButton(
                    text = templateUi.shortText,
                    modifier = Modifier
                        .clip(squircleShape)
                        .animateItem()
                        .combinedClickable(
                            onClick = {
                                onDone(templateUi)
                            },
                            onLongClick = {
                                navigationFs.push {
                                    EventTemplateFormFs(
                                        initEventTemplateDb = templateUi.eventTemplateDb,
                                    )
                                }
                            },
                        ),
                )
            }
        }

        item(key = "add_template") {
            ListButton(
                text = state.newTemplateText,
                modifier = Modifier
                    .clip(squircleShape)
                    .clickable {
                        navigationFs.push {
                            EventTemplateFormFs(
                                initEventTemplateDb = null,
                            )
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
