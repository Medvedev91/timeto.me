package me.timeto.app.ui.form.sorted

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.ZStack

@Composable
fun <Item> FormSortedList(
    items: List<Item>,
    itemId: (Item) -> Int,
    itemTitle: (Item) -> String,
    onItemClick: (Item) -> Unit,
    onItemLongClick: ((Item) -> Unit)?,
    onItemDelete: ((Item) -> Unit)?,
    scrollState: LazyListState,
    modifier: Modifier,
    onMove: (Int, Int) -> Unit, // from idx / to idx
    onFinish: () -> Unit,
) {

    val sortedState = remember {
        FormSortedState()
    }
    val sortedMovingIdx = remember {
        mutableStateOf<Int?>(null)
    }

    LazyColumn(
        modifier = modifier,
        state = scrollState,
        userScrollEnabled = sortedMovingIdx.value == null,
    ) {

        item {
            ZStack(Modifier.height(4.dp))
        }

        items.forEachIndexed { idx, item ->
            item(key = "item_${itemId(item)}") {
                FormSortedItemView(
                    title = itemTitle(item),
                    isFirst = items.first() == item,
                    itemIdx = idx,
                    sortedState = sortedState,
                    sortedMovingIdx = sortedMovingIdx,
                    onMoveProcess = { fromIdx, toIdx ->
                        onMove(fromIdx, toIdx)
                    },
                    onMoveFinish = {
                        onFinish()
                    },
                    onClick = {
                        onItemClick(item)
                    },
                    onLongClick = onItemLongClick?.let {
                        { onItemLongClick(item) }
                    },
                    onDelete = onItemDelete?.let {
                        { onItemDelete(item) }
                    },
                )
            }
        }
    }
}
