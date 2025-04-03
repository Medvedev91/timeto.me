package me.timeto.app.ui.tasks.folders

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterAddButton
import me.timeto.app.ui.footer.FooterRightButton
import me.timeto.app.ui.form.FormSortedList
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.ui.tasks.TaskFoldersFormVm

@Composable
fun TaskFoldersFormFs() {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        TaskFoldersFormVm()
    }

    Screen(
        modifier = Modifier
            .imePadding(),
    ) {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = null,
            cancelButton = HeaderCancelButton(
                text = "Close",
                onClick = {
                    navigationLayer.close()
                },
            )
        )

        fun openTaskFolderFormFs(taskFolderDb: TaskFolderDb) {
            navigationFs.push {
                TaskFolderFormFs(
                    initTaskFolderDb = taskFolderDb,
                )
            }
        }

        FormSortedList(
            items = state.foldersDb,
            itemId = { it.id },
            itemTitle = { it.name },
            onItemClick = { taskFolderDb ->
                openTaskFolderFormFs(taskFolderDb)
            },
            onItemLongClick = { taskFolderDb ->
                openTaskFolderFormFs(taskFolderDb)
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
            }
        )

        Footer(
            scrollState = scrollState,
            contentModifier = Modifier
                .padding(horizontal = H_PADDING_HALF)
        ) {

            FooterAddButton(
                text = "New Folder",
                onClick = {
                    navigationFs.push {
                        TaskFolderFormFs(
                            initTaskFolderDb = null,
                        )
                    }
                },
            )

            SpacerW1()

            val tmrwButtonUi = state.tmrwButtonUi
            if (tmrwButtonUi != null) {
                FooterRightButton(
                    text = tmrwButtonUi.text,
                    onClick = {
                        tmrwButtonUi.add(
                            dialogsManager = navigationFs,
                        )
                    },
                )
            }
        }
    }
}
