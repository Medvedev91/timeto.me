package me.timeto.app.ui.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.*
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.TaskDb
import me.timeto.shared.vm.tasks.TaskTimerVm

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskTimerFs(
    taskDb: TaskDb,
) {

    val navigationLayer = LocalNavigationLayer.current

    val (_, state) = rememberVm {
        TaskTimerVm(
            taskDb = taskDb,
        )
    }

    Screen(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight(),
            reverseLayout = true,
        ) {

            val goalsUi = state.goalsUi.reversed()
            goalsUi.forEach { goalUi ->
                item {

                    ZStack(
                        contentAlignment = Alignment.BottomCenter, // for divider
                    ) {

                        HStack(
                            modifier = Modifier
                                .height(42.dp)
                                .clickable {
                                    goalUi.onTap()
                                    navigationLayer.close()
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            Text(
                                text = goalUi.text,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .weight(1f),
                                color = c.text,
                                textAlign = TextAlign.Start,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )

                            HStack(
                                modifier = Modifier
                                    .padding(end = 2.dp),
                            ) {
                                goalUi.timerHintsUi.forEach { timerHintUi ->
                                    Text(
                                        text = timerHintUi.title,
                                        modifier = Modifier
                                            .clip(roundedShape)
                                            .clickable {
                                                timerHintUi.onTap()
                                                navigationLayer.close()
                                            }
                                            .padding(horizontal = 8.dp),
                                        color = c.blue,
                                    )
                                }
                            }
                        }

                        if (goalsUi.first() != goalUi) {
                            Divider(Modifier.padding(start = H_PADDING))
                        }
                    }
                }
            }
        }
    }
}
