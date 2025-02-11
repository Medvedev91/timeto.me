package me.timeto.app.ui.checklists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.timeto.app.VStack
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.FormPaddingFirstItem
import me.timeto.app.ui.header.HeaderAction
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.ui.checklists.ChecklistFormVm

@Composable
fun ChecklistFormFs(
    checklistDb: ChecklistDb?,
    onSave: (ChecklistDb) -> Unit,
    onDelete: () -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ChecklistFormVm(
            checklistDb = checklistDb,
        )
    }

    VStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg),
    ) {

        val scrollState = rememberLazyListState()

        HeaderAction(
            title = state.title,
            actionText = state.saveText,
            isEnabled = state.isSaveEnabled,
            scrollState = null,
            onCancel = {
                navigationLayer.close()
            },
            onDone = {
                vm.save(
                    dialogsManager = navigationFs,
                    onSuccess = { newChecklistDb ->
                        onSave(newChecklistDb)
                        navigationLayer.close()
                    },
                )
            },
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
            contentPadding = PaddingValues(bottom = 25.dp),
        ) {

            item {
                FormPaddingFirstItem()
            }

            item {
                FormInput(
                    initText = state.name,
                    placeholder = state.namePlaceholder,
                    onChange = { newName ->
                        vm.setName(newName)
                    },
                    isFirst = true,
                    isLast = true,
                    isAutoFocus = true,
                    imeAction = ImeAction.Done,
                )
            }
        }
    }
}
