package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.vm.ChecklistSheetVM

@Composable
fun ChecklistSheet(
    layer: WrapperView.Layer,
    checklist: ChecklistDb,
) {
    val (_, state) = rememberVM { ChecklistSheetVM(checklist) }

    VStack(
        modifier = Modifier
            .background(c.bg),
    ) {

        Text(
            checklist.name,
            modifier = Modifier
                .padding(horizontal = H_PADDING)
                .padding(top = 24.dp, bottom = 12.dp),
            color = c.text,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        val scrollState = rememberLazyListState()

        DividerBg(
            modifier = Modifier
                .padding(horizontal = H_PADDING),
            isVisible = remember {
                derivedStateOf { scrollState.canScrollBackward }
            }.value,
        )

        ChecklistView(
            checklistDb = state.checklistDb,
            modifier = Modifier
                .weight(1f),
            scrollState = scrollState,
            onDelete = { layer.close() },
            maxLines = 9,
            bottomPadding = 32.dp,
        )

        DividerBg(
            modifier = Modifier
                .padding(horizontal = H_PADDING)
                .navigationBarsPadding(),
            isVisible = remember {
                derivedStateOf { scrollState.canScrollForward }
            }.value,
        )
    }
}
