package me.timeto.app.ui.checklists

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.Screen
import me.timeto.app.ui.checklists.form.ChecklistFormItemsFs
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.header.HeaderSecondaryButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.ChecklistDb

@Composable
fun ChecklistScreen(
    checklistDb: ChecklistDb,
    withNavigationPadding: Boolean,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = checklistDb.name,
            scrollState = scrollState,
            actionButton = null,
            cancelButton = HeaderCancelButton(
                text = "Close",
                onClick = {
                    navigationLayer.close()
                },
            ),
            secondaryButtons = listOf(
                HeaderSecondaryButton(
                    text = "Edit",
                    onClick = {
                        navigationFs.push {
                            ChecklistFormItemsFs(
                                checklistDb = checklistDb,
                                onDelete = {
                                    navigationLayer.close()
                                },
                            )
                        }
                    },
                )
            ),
        )

        ChecklistView(
            checklistDb = checklistDb,
            modifier = Modifier,
            scrollState = scrollState,
            maxLines = Int.MAX_VALUE,
            withAddButton = true,
            topPadding = 5.dp,
            bottomPadding = 16.dp,
            withNavigationPadding = withNavigationPadding,
        )
    }
}
