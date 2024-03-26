package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.timeto.app.c
import me.timeto.shared.UIConfirmationData

@Composable
fun ConfirmationDialogView(
    data: UIConfirmationData,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(c.sheetBg)
            .padding(20.dp)
    ) {

        Text(
            text = data.text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp),
            color = c.text,
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
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

            MyButton(data.buttonText, true, if (data.isRed) c.red else c.blue) {
                data.onConfirm()
                onClose()
            }
        }
    }
}
