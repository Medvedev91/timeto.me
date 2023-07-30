package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.c
import me.timeto.app.rememberVM
import me.timeto.shared.db.ChecklistItemModel
import me.timeto.shared.db.ChecklistModel
import me.timeto.shared.vm.ChecklistItemFormVM

@Composable
fun ChecklistItemEditDialog(
    checklist: ChecklistModel,
    editedChecklistItem: ChecklistItemModel?,
    onClose: () -> Unit,
) {

    val (vm, state) = rememberVM(checklist, editedChecklistItem) { ChecklistItemFormVM(checklist, editedChecklistItem) }

    Column(
        modifier = Modifier
            .background(c.bgFormSheet)
            .padding(20.dp)
    ) {

        Text(
            editedChecklistItem?.text ?: "New Item",
            modifier = Modifier
                .padding(start = 10.dp, bottom = 15.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.W500,
        )

        //
        //

        MyListView__ItemView(
            isFirst = true,
            isLast = true,
            outerPadding = PaddingValues(horizontal = 8.dp)
        ) {
            MyListView__ItemView__TextInputView(
                placeholder = "Item",
                text = state.inputNameValue,
                onTextChanged = { vm.setInputName(it) },
                isAutofocus = true,
            )
        }

        ////

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                "Cancel",
                color = c.textSecondary,
                modifier = Modifier
                    .padding(end = 11.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClose() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            MyButton("Save", state.isSaveEnabled, c.blue) {
                vm.save {
                    onClose()
                }
            }
        }
    }
}
