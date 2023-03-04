package app.time_to.timeto.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.*
import app.time_to.timeto.R
import timeto.shared.db.ChecklistModel
import timeto.shared.launchEx
import timeto.shared.vm.ChecklistDialogVM

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChecklistDialogView(
    checklist: ChecklistModel,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val (_, state) = rememberVM { ChecklistDialogVM(checklist = checklist) }

    val isChecklistItemAddPresented = remember { mutableStateOf(false) }
    ChecklistItemEditDialog(isChecklistItemAddPresented, checklist = checklist, editedChecklistItem = null)
    val checklistItems = state.items

    Box(Modifier.background(c.background)) {

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 70.dp, bottom = 75.dp)
        ) {

            itemsIndexed(checklistItems, key = { _, item -> item.id }) { _, item ->

                val isChecklistItemEditPresented = remember { mutableStateOf(false) }
                ChecklistItemEditDialog(isChecklistItemEditPresented, checklist = checklist, editedChecklistItem = item)

                MyListView__ItemView(
                    isFirst = checklistItems.first() == item,
                    isLast = checklistItems.last() == item,
                    withTopDivider = checklistItems.first() != item,
                ) {

                    SwipeToAction(
                        isStartOrEnd = remember { mutableStateOf(null) },
                        startView = {
                            SwipeToAction__StartView(
                                text = "Edit",
                                bgColor = c.blue
                            )
                        },
                        endView = { state ->
                            SwipeToAction__DeleteView(state, checklist.name) {
                                vibrateLong()
                                scope.launchEx {
                                    item.delete()
                                }
                            }
                        },
                        onStart = {
                            isChecklistItemEditPresented.value = true
                            false
                        },
                        onEnd = {
                            true
                        },
                        toVibrateStartEnd = listOf(true, false),
                    ) {
                        Box {
                            MyListView__ItemView__ButtonView(
                                text = item.text,
                                textModifier = Modifier.padding(
                                    end = 40.dp, // Check icon space
                                )
                            ) {
                                scope.launchEx {
                                    item.toggle()
                                }
                            }
                            Column(
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                AnimatedVisibility(
                                    visible = item.isChecked(),
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                ) {
                                    Icon(
                                        painterResource(id = R.drawable.sf_checkmark_medium_medium),
                                        "Checkmark",
                                        tint = c.blue,
                                        modifier = Modifier
                                            .padding(end = 18.5.dp)
                                            .size(18.dp)
                                            .alpha(0.8f)
                                            .clip(RoundedCornerShape(99.dp))
                                            .padding(3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            checklist.name,
            modifier = Modifier
                .padding(start = 38.dp, end = 30.dp, top = 20.dp),
            color = c.text,
            fontSize = 24.sp,
            fontWeight = FontWeight.W500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 21.dp, bottom = 20.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {

            AnimatedVisibility(
                visible = checklistItems.any { it.isChecked() },
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Text(
                    "Uncheck",
                    modifier = Modifier
                        .padding(start = 29.dp)
                        .clip(MySquircleShape())
                        .background(c.background)
                        .clickable {
                            scope.launchEx {
                                checklistItems
                                    .filter { it.isChecked() }
                                    .forEach { it.toggle() }
                            }
                        }
                        .padding(bottom = 5.dp, top = 5.dp, start = 9.dp, end = 9.dp),
                    color = c.textSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W400
                )
            }

            SpacerW1()

            Text(
                "Close",
                modifier = Modifier
                    .padding(end = 14.dp)
                    .clip(MySquircleShape())
                    .clickable {
                        onClose()
                    }
                    .padding(bottom = 5.dp, top = 5.dp, start = 9.dp, end = 9.dp),
                color = c.textSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.W400
            )

            MyButton(
                "New Item",
                true,
                c.blue,
            ) {
                isChecklistItemAddPresented.value = true
            }
        }
    }
}
