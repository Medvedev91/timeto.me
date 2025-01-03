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
import me.timeto.app.HStack
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.vm.ChecklistNameDialogVm

@Composable
fun ChecklistNameDialog(
    layer: WrapperView.Layer,
    editedChecklist: ChecklistDb?,
    onSave: (ChecklistDb) -> Unit,
) {

    val (vm, state) = rememberVm(editedChecklist) {
        ChecklistNameDialogVm(editedChecklist)
    }

    Column(
        modifier = Modifier
            .background(c.sheetBg)
            .padding(20.dp)
    ) {

        Text(
            text = state.title,
            modifier = Modifier
                .padding(start = 10.dp, bottom = 15.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.W500,
            color = c.text,
        )

        //
        //

        MyListView__ItemView(
            isFirst = true,
            isLast = true,
            outerPadding = PaddingValues(horizontal = 8.dp)
        ) {
            MyListView__ItemView__TextInputView(
                placeholder = state.placeholder,
                text = state.input,
                onTextChanged = { vm.setInput(it) },
                isAutofocus = true,
            )
        }

        ////

        HStack(
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
                    .clickable {
                        layer.close()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            MyButton("Save", state.isSaveEnabled, c.blue) {
                vm.save { checklist ->
                    layer.close()
                    onSave(checklist)
                }
            }
        }
    }
}
