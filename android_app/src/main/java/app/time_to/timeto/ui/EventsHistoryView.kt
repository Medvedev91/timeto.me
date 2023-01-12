package app.time_to.timeto.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.*
import timeto.shared.vm.EventsHistoryVM

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventsHistoryView(
    spaceAround: Dp,
    paddingTop: Dp,
) {

    val (vm, state) = rememberVM { EventsHistoryVM() }
    val uiItems = state.uiItems

    val scrollState = rememberLazyListState()
    LaunchedEffect(uiItems.firstOrNull()?.historyItem?.normalized_title) {
        if (uiItems.isNotEmpty())
            scrollState.animateScrollToItem(0)
    }

    LazyRow(
        modifier = Modifier
            .padding(top = if (state.uiItems.isEmpty()) 0.dp else paddingTop),
        contentPadding = PaddingValues(horizontal = spaceAround),
        state = scrollState
    ) {

        itemsIndexed(
            uiItems,
            key = { _, item -> item.historyItem.normalized_title }
        ) { _, uiItem ->

            val isAddEventPresented = remember { mutableStateOf(false) }
            EventFormSheet(
                isPresented = isAddEventPresented,
                editedEvent = null,
                defText = uiItem.historyItem.raw_title,
                defTime = uiItem.defTime,
            ) {}

            Text(
                uiItem.note,
                modifier = Modifier
                    .padding(end = if (uiItem == uiItems.last()) 0.dp else 8.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(c.blue)
                    .padding(1.dp)
                    .animateItemPlacement()
                    .combinedClickable(
                        onClick = {
                            isAddEventPresented.value = true
                        },
                        onLongClick = {
                            vm.delItem(uiItem.historyItem)
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
