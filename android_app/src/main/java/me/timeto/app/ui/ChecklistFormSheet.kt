package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.vm.ChecklistFormSheetVM

@Composable
fun ChecklistFormSheet(
    layer: WrapperView.Layer,
    checklistDb: ChecklistDb,
) {

    val (_, state) = rememberVM(checklistDb) {
        ChecklistFormSheetVM(checklistDb)
    }

    VStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.sheetBg),
    ) {

        HStack(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 4.dp)
                .padding(horizontal = H_PADDING_HALF)
                .clip(squircleShape)
                .clickable {
                }
                .padding(horizontal = H_PADDING_HALF, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = state.checklistName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = c.white,
            )

            SpacerW1()

            Icon(
                painterResource(R.drawable.sf_pencil_medium_medium),
                contentDescription = "Edit Name",
                tint = c.white,
                modifier = Modifier
                    .offset(y = 1.dp)
                    .padding(start = 16.dp, end = 2.dp)
                    .size(20.dp),
            )
        }

        DividerFg(Modifier.padding(horizontal = H_PADDING))
    }
}
