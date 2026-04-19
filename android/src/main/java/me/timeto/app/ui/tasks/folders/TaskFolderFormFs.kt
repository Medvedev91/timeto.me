package me.timeto.app.ui.tasks.folders

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import me.timeto.app.ui.c
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.navigation.picker.NavigationPickerItem
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.vm.tasks.folders.TaskFolderFormVm

@Composable
fun TaskFolderFormFs(
    initTaskFolderDb: TaskFolderDb?,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        TaskFolderFormVm(
            folderDb = initTaskFolderDb,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = state.doneText,
                isEnabled = state.isSaveEnabled,
                onClick = {
                    vm.save(
                        dialogsManager = navigationFs,
                        onSuccess = {
                            navigationLayer.close()
                        },
                    )
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
                .fillMaxSize(),
            state = scrollState,
        ) {

            item {

                FormPaddingTop()

                if (state.activityDb == null) {
                    FormInput(
                        initText = state.name,
                        placeholder = state.namePlaceholder,
                        onChange = { newName ->
                            vm.setName(newName)
                        },
                        isFirst = true,
                        isLast = true,
                        isAutoFocus = false,
                        imeAction = ImeAction.Done,
                    )
                }

                if (state.isActivityAvailable) {
                    if (state.activityDb == null)
                        FormPaddingSectionSection()
                    FormButton(
                        title = state.activityTitle,
                        isFirst = true,
                        isLast = true,
                        note = state.activityNote ?: "None",
                        withArrow = true,
                        onClick = {
                            navigationFs.picker(
                                title = state.activityTitle,
                                items = buildActivitiesPickerItems(
                                    activitiesUi = state.activitiesUi,
                                    selectedActivityDb = state.activityDb,
                                ),
                                onDone = { pickerItem ->
                                    vm.setActivity(pickerItem.item)
                                },
                            )
                        },
                    )
                }

                val taskFolderDb: TaskFolderDb? = state.folderDb
                if (taskFolderDb != null) {
                    FormPaddingSectionSection()
                    FormButton(
                        title = state.deleteText,
                        titleColor = c.red,
                        isFirst = true,
                        isLast = true,
                        onClick = {
                            vm.delete(
                                folderDb = taskFolderDb,
                                dialogsManager = navigationFs,
                                onDelete = {
                                    navigationLayer.close()
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}

private fun buildActivitiesPickerItems(
    activitiesUi: List<TaskFolderFormVm.ActivityUi>,
    selectedActivityDb: ActivityDb?,
): List<NavigationPickerItem<ActivityDb?>> {
    val list = mutableListOf<NavigationPickerItem<ActivityDb?>>()
    list.add(
        NavigationPickerItem(
            title = "None",
            isSelected = selectedActivityDb == null,
            item = null,
        )
    )
    activitiesUi.forEach { activityUi ->
        list.add(
            NavigationPickerItem(
                title = activityUi.title,
                isSelected = selectedActivityDb?.id == activityUi.activityDb.id,
                item = activityUi.activityDb,
            )
        )
    }
    return list
}
