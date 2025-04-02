package me.timeto.app.ui.activities

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.activities.form.ActivityFormFs
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterAddButton
import me.timeto.app.ui.form.FormSortedList
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.ui.activities.ActivitiesFormVm

@Composable
fun ActivitiesFormFs() {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ActivitiesFormVm()
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    navigationLayer.close()
                },
            ),
            cancelButton = null,
        )

        fun openActivityFormFs(activityDb: ActivityDb) {
            navigationFs.push {
                ActivityFormFs(
                    initActivityDb = activityDb,
                )
            }
        }

        FormSortedList(
            items = state.activitiesUi,
            itemId = { it.activityDb.id },
            itemTitle = { it.title },
            onItemClick = { activityUi ->
                openActivityFormFs(activityUi.activityDb)
            },
            onItemLongClick = { activityUi ->
                openActivityFormFs(activityUi.activityDb)
            },
            onItemDelete = null,
            scrollState = scrollState,
            modifier = Modifier
                .weight(1f),
            onMove = { fromIdx, toIdx ->
                vm.moveAndroidLocal(fromIdx, toIdx)
            },
            onFinish = {
                vm.moveAndroidSync()
            },
        )

        Footer(
            scrollState = scrollState,
            contentModifier = Modifier
                .padding(horizontal = H_PADDING_HALF),
        ) {
            FooterAddButton(
                text = "New Activity",
                onClick = {
                    navigationFs.push {
                        ActivityFormFs(
                            initActivityDb = null,
                        )
                    }
                },
            )
            SpacerW1()
        }
    }
}
