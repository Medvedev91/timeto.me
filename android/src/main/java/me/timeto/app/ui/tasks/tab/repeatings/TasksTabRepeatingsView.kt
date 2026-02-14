package me.timeto.app.ui.tasks.tab.repeatings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.c
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.repeatings.form.RepeatingFormFs
import me.timeto.app.ui.squircleShape
import me.timeto.app.ui.tasks.tab.TasksTabView__LIST_SECTION_PADDING
import me.timeto.app.ui.tasks.tab.TasksTabView__PADDING_END
import me.timeto.shared.vm.tasks.tab.repeatings.TasksTabRepeatingsVm

@Composable
fun TasksTabRepeatingsView() {

    val navigationFs = LocalNavigationFs.current

    val (_, state) = rememberVm {
        TasksTabRepeatingsVm()
    }

    LazyColumn(
        reverseLayout = true,
        contentPadding = PaddingValues(
            end = TasksTabView__PADDING_END,
            bottom = TasksTabView__LIST_SECTION_PADDING,
            top = TasksTabView__LIST_SECTION_PADDING,
        ),
        modifier = Modifier
            .fillMaxHeight(),
    ) {

        item {

            Text(
                text = "New Repeating Task",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = H_PADDING - 2.dp)
                    .padding(top = TasksTabView__LIST_SECTION_PADDING)
                    .clip(squircleShape)
                    .background(c.blue)
                    .clickable {
                        navigationFs.push {
                            RepeatingFormFs(
                                initRepeatingDb = null,
                            )
                        }
                    }
                    .padding(vertical = 10.dp),
                color = c.white,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }

        val repeatingsUi = state.repeatingsUi
        repeatingsUi.forEachIndexed { idx, repeatingUi ->
            item {
                TasksTabRepeatingsItemView(
                    repeatingUi = repeatingUi,
                    // Remember that the list is reversed
                    withTopDivider = (idx != repeatingsUi.size - 1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = H_PADDING_HALF),
                )
            }
        }
    }
}
