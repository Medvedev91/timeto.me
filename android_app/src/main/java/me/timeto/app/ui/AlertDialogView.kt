package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.shared.UIAlertData

@Composable
fun AlertDialogView(
    data: UIAlertData,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(c.background2)
            .padding(20.dp)
    ) {

        Text(
            text = data.message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            MyButton("OK", true, c.blue) {
                onClose()
            }
        }
    }
}
