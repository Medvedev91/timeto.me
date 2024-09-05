package me.timeto.app.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.*
import kotlin.math.absoluteValue

@Composable
fun <T> MyPicker(
    items: List<T>,
    containerWidth: Dp,
    containerHeight: Dp,
    itemHeight: Dp,
    selectedIndex: Int,
    onChange: (index: Int, item: T) -> Unit, // WARNING Update selectedIndex
) {
    val listState = rememberLazyListState()

    val containerHeightPx = dpToPx(containerHeight.value)
    val itemHeightPx = dpToPx(itemHeight.value)

    val vContentPadding = (containerHeight - itemHeight) / 2

    val idealItemsScrollPx = items.indices.map { it * itemHeightPx + containerHeightPx / 2 }

    ///
    /// Initially without animation
    LaunchedEffect(Unit) {
        listState.scrollToItem(selectedIndex)
    }
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }
    //////

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress)
            return@LaunchedEffect

        val totalScroll = listState.firstVisibleItemIndex * itemHeightPx + listState.firstVisibleItemScrollOffset + containerHeightPx / 2
        val nearestIndex = idealItemsScrollPx.indexOf(idealItemsScrollPx.minByOrNull { (totalScroll - it).absoluteValue }!!)

        if (nearestIndex != selectedIndex) {
            onChange(nearestIndex, items[nearestIndex])
            // To not forget to update selectedIndex inside onChange()
            listState.animateScrollToItem(selectedIndex)
        }
        // In case for small change. Rollback if the same index
        else if (listState.firstVisibleItemScrollOffset != 0)
            listState.animateScrollToItem(selectedIndex)
    }

    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = c.text,
                shape = squircleShape,
            )
            .clip(squircleShape)
            .size(containerWidth, containerHeight),
    ) {

        LazyColumn(
            modifier = Modifier
                /**
                 * Based on https://stackoverflow.com/a/68738725/5169420. Linked methods.
                 */
                .graphicsLayer {
                    // https://stackoverflow.com/a/68738725/5169420 "By default Jetpack..."
                    // Known issues:
                    // - Background changes from transparent to target, in the case below (Color.Black);
                    // - On expand inside the repeating form it's rendered in parts.
                    alpha = 0.99F
                }
                .drawWithContent {
                    drawContent()
                    // There is an option to use fade only if the height of the block is greater
                    // than the height of the item. Although right now it looks like a wheel.
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.4f to Color.Black
                        ),
                        blendMode = BlendMode.DstIn
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.6f to Color.Transparent,
                            1.0f to Color.Black
                        ),
                        blendMode = BlendMode.DstOut
                    )
                }
                /****/
                .fillMaxWidth()
                .fillMaxHeight(),
            state = listState,
            contentPadding = PaddingValues(vertical = vContentPadding)
        ) {

            items(items) { item ->

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item.toString(),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .offset(y = -(1.5.dp)),
                        color = c.text
                    )
                }
            }
        }
    }
}
