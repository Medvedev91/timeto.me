package me.timeto.app.ui.activities

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.ui.ZStack
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.plain.FormPlainButtonSelection
import me.timeto.app.ui.form.plain.FormPlainPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.ui.activities.ActivityPickerVm

@Composable
fun ActivityPickerFs(
    initActivityDb: ActivityDb?,
    onDone: (ActivityDb) -> Unit,
) {

    val navigationLayer = LocalNavigationLayer.current

    val (_, state) = rememberVm {
        ActivityPickerVm()
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = "Activity",
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = initActivityDb != null,
                onClick = {
                    navigationLayer.close()
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f),
            state = scrollState,
        ) {

            item {
                FormPlainPaddingTop()
            }

            state.activitiesUi.forEachIndexed { idx, activityUi ->
                val activityDb: ActivityDb = activityUi.activityDb
                item(key = activityDb.id) {
                    FormPlainButtonSelection(
                        title = activityUi.title,
                        isSelected = activityDb.id == initActivityDb?.id,
                        isFirst = idx == 0,
                        modifier = Modifier
                            .animateItem(),
                        onClick = {
                            onDone(activityDb)
                            navigationLayer.close()
                        },
                    )
                }
            }

            item {
                ZStack(Modifier.navigationBarsPadding())
            }
        }
    }
}
