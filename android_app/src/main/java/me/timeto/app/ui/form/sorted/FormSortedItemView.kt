package me.timeto.app.ui.form.sorted

import android.view.MotionEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.HStack
import me.timeto.app.H_PADDING
import me.timeto.app.R
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.goldenRatioDown
import me.timeto.app.mics.Haptic
import me.timeto.app.roundedShape
import me.timeto.app.ui.Divider
import me.timeto.app.ui.form.form__itemMinHeight
import kotlin.math.absoluteValue

private val deleteIconSize: Dp = 20.dp
private val deleteIconTapAreaPadding: Dp = 4.dp
private val deleteIconLeadingPadding: Dp = H_PADDING - deleteIconTapAreaPadding
private val deleteIconTrailingPadding: Dp = H_PADDING.goldenRatioDown()
private val deleteDividerPadding: Dp = deleteIconSize + deleteIconTrailingPadding + 1.dp

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.FormSortedItemView(
    title: String,
    isFirst: Boolean,
    itemIdx: Int,
    sortedState: FormSortedState,
    sortedMovingIdx: MutableState<Int?>,
    onMoveProcess: (Int, Int) -> Unit,
    onMoveFinish: () -> Unit,
    onDelete: (() -> Unit)?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
) {

    DisposableEffect(Unit) {
        onDispose {
            sortedState.idxToYMap.remove(itemIdx)
        }
    }

    ZStack(
        modifier = Modifier
            .animateItem()
            .fillMaxWidth()
            .background(c.bg),
    ) {

        HStack(
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .sizeIn(minHeight = form__itemMinHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            if (onDelete != null) {
                ZStack(
                    modifier = Modifier
                        .padding(start = deleteIconLeadingPadding)
                        .clip(roundedShape)
                        .clickable {
                            onDelete()
                        }
                        .padding(deleteIconTapAreaPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    ZStack(
                        modifier = Modifier
                            .size(deleteIconSize - 2.dp)
                            .clip(roundedShape)
                            .background(c.white),
                    ) {}
                    Icon(
                        painter = painterResource(id = R.drawable.sf_minus_circle_fill_medium_regular),
                        contentDescription = "Delete",
                        tint = c.red,
                        modifier = Modifier
                            .size(deleteIconSize),
                    )
                }
            }

            Text(
                title,
                modifier = Modifier
                    .padding(start = H_PADDING)
                    .weight(1f),
                color = c.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            HStack(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        val y: Int = coordinates.positionInWindow().y.toInt()
                        val height: Int = coordinates.size.height
                        if (sortedMovingIdx.value == null) {
                            sortedState.idxToYMap[itemIdx] = (y + (height / 2))
                        }
                    }
                    .height(form__itemMinHeight)
                    .motionEventSpy { event ->
                        if (sortedMovingIdx.value == null) {
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                sortedMovingIdx.value = itemIdx
                                hapticFeedback()
                            }
                            return@motionEventSpy
                        }
                        if (event.action == MotionEvent.ACTION_UP) {
                            sortedMovingIdx.value = null
                            onMoveFinish()
                        } else if (event.action == MotionEvent.ACTION_MOVE) {
                            val newIdx: Int? = sortedState.idxToYMap
                                .map { it.key to (it.value - event.y).absoluteValue }
                                .minByOrNull { it.second }
                                ?.first
                            if ((newIdx != null) && (newIdx != itemIdx)) {
                                onMoveProcess(itemIdx, newIdx)
                                hapticFeedback()
                            }
                        } else {
                            // Other action like many taps
                            sortedMovingIdx.value = null
                            onMoveFinish()
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.sf_line_3_horizontal_medium_light),
                    contentDescription = "Sort",
                    tint = c.textSecondary,
                    modifier = Modifier
                        .padding(end = H_PADDING)
                        .padding(start = H_PADDING) // To tap area
                        .size(20.dp)
                )
            }
        }

        if (!isFirst) {
            val extraDividerPadding: Dp =
                if (onDelete == null) 0.dp
                else deleteDividerPadding + deleteIconTrailingPadding
            Divider(
                modifier = Modifier
                    .padding(start = H_PADDING + extraDividerPadding),
            )
        }
    }
}

///

private fun hapticFeedback() {
    Haptic.shot()
}
