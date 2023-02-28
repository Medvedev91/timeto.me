package app.time_to.timeto.ui

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
import app.time_to.timeto.rememberVM
import timeto.shared.db.ChecklistItemModel
import timeto.shared.db.ChecklistModel
import timeto.shared.vm.ChecklistItemFormVM

@Composable
fun ChecklistItemEditDialog(
    isPresented: MutableState<Boolean>,
    checklist: ChecklistModel,
    editedChecklistItem: ChecklistItemModel?,
) {

    MyDialog(
        isPresented,
        backgroundColor = c.bgFormSheet
    ) {

        val (vm, state) = rememberVM(checklist, editedChecklistItem) { ChecklistItemFormVM(checklist, editedChecklistItem) }

        Column {

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
                        .clickable { isPresented.value = false }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                MyButton("Save", state.isSaveEnabled, c.blue) {
                    vm.save {
                        isPresented.value = false
                    }
                }
            }
        }
    }
}
