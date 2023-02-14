package app.time_to.timeto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.rememberVM
import app.time_to.timeto.toColor
import timeto.shared.vm.TmrwPeekVM

@Composable
fun TmrwPeekView() {

    val (_, state) = rememberVM { TmrwPeekVM() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = TAB_TASKS_PADDING_START, end = TAB_TASKS_PADDING_END),
            reverseLayout = true,
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        ) {

            item {
                Box(
                    modifier = Modifier
                        .padding(start = TAB_TASKS_PADDING_START)
                        .fillMaxWidth()
                        .padding(top = taskListSectionPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.curTimeString,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W300,
                        color = c.textSecondary,
                    )
                }
            }

            val tasksUI = state.tasksUI
            items(
                tasksUI,
                key = { taskUI -> taskUI.task.id }
            ) { taskUI ->

                val startPadding = 18.dp

                // Reversed
                val isFirst = taskUI == tasksUI.lastOrNull()
                val isLast = taskUI == tasksUI.firstOrNull()

                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = isLast,
                    withTopDivider = !isFirst,
                    outerPadding = PaddingValues(0.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {

                        val vPadding = 6.dp

                        val daytimeUI = taskUI.textFeatures.timeUI
                        if (daytimeUI != null) {
                            Text(
                                daytimeUI.daytimeText,
                                modifier = Modifier
                                    .padding(
                                        start = startPadding,
                                        top = 2.dp,
                                        bottom = vPadding,
                                    ),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W300,
                                color = daytimeUI.color.toColor(),
                            )
                        }

                        Text(
                            taskUI.listText,
                            color = c.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = startPadding),
                        )

                        TriggersView__ListView(
                            triggers = taskUI.textFeatures.triggers,
                            withOnClick = true,
                            modifier = Modifier.padding(top = vPadding),
                            contentPadding = PaddingValues(horizontal = startPadding - 2.dp),
                        )
                    }
                }
            }
        }
    }
}
