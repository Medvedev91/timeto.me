package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.rememberVM
import me.timeto.app.roundedShape
import me.timeto.shared.vm.EditActivitiesVM

@Composable
fun EditActivitiesSheet(
    layer: WrapperView.Layer
) {

    val (vm, state) = rememberVM { EditActivitiesVM() }

    Box(
        modifier = Modifier
            .background(c.background)
            .navigationBarsPadding()
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 70.dp)
        ) {

            val activitiesUI = state.activitiesUI
            itemsIndexed(
                activitiesUI,
                key = { _, item -> item.activity.id }
            ) { _, activityUI ->

                val isFirst = activitiesUI.first() == activityUI

                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = activitiesUI.last() == activityUI,
                    withTopDivider = !isFirst,
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(c.background2),
                        contentAlignment = Alignment.TopCenter
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                activityUI.listText,
                                modifier = Modifier
                                    .padding(
                                        PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 12.dp,
                                        )
                                    )
                                    .weight(1f),
                                color = c.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Icon(
                                Icons.Rounded.ArrowDownward,
                                "Down",
                                tint = c.blue,
                                modifier = Modifier
                                    .padding(start = 5.dp)
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .clickable {
                                        vm.down(activityUI)
                                    }
                                    .padding(1.dp)
                            )

                            Icon(
                                Icons.Rounded.ArrowUpward,
                                "Up",
                                tint = c.blue,
                                modifier = Modifier
                                    .padding(start = 4.dp, end = 8.dp)
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .clickable {
                                        vm.up(activityUI)
                                    }
                                    .padding(1.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 8.dp, bottom = 20.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "New Activity",
                modifier = Modifier
                    .clip(roundedShape)
                    .background(c.blue)
                    .clickable {
                        Sheet.show { layer ->
                            ActivityFormSheet(layer = layer, editedActivity = null)
                        }
                    }
                    .padding(bottom = 5.dp, top = 5.dp, start = 12.dp, end = 12.dp),
                color = c.white,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )

            SpacerW1()

            Text(
                "Close",
                modifier = Modifier
                    .padding(end = 14.dp)
                    .clip(MySquircleShape())
                    .clickable {
                        layer.close()
                    }
                    .padding(bottom = 5.dp, top = 5.dp, start = 9.dp, end = 9.dp),
                color = c.textSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.W400
            )
        }
    }
}
