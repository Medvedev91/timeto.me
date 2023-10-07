package me.timeto.app.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.ChartVM

@Composable
fun ChartDialogView() {

    val (vm, state) = rememberVM { ChartVM() }

    Box(
        modifier = Modifier.background(c.sheetBg)
    ) {

        Column {

            Box(
                modifier = Modifier
                    .padding(top = 28.dp, start = 40.dp, end = 40.dp)
                    .align(Alignment.CenterHorizontally)
                    // ChartUI() must be in square
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {

                WyouChart.ChartUI(state.pieItems, state.selectedId) {
                    vm.selectId(it)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    bottom = 12.dp,
                    top = 26.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                itemsIndexed(state.pieItems) { _, pie ->
                    val curId = pie.id
                    Row(
                        modifier = Modifier
                            .height(IntrinsicSize.Min) // To use fillMaxHeight() inside
                            .padding(start = 2.dp)
                            .clip(squircleShape)
                            .clickable {
                                vm.selectId(if (state.selectedId == curId) null else curId)
                            }
                            .padding(start = 6.dp, end = 1.dp, top = 5.dp, bottom = 5.dp)
                    ) {
                        val width = animateDpAsState(
                            if (state.selectedId == curId) 23.dp else 10.dp,
                            spring(stiffness = Spring.StiffnessLow)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(width.value)
                                .clip(RoundedCornerShape(5.dp))
                                .background(pie.color.toColor())
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                        ) {

                            Text(
                                pie.title,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.W500,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = c.text,
                            )

                            Row {

                                Text(
                                    pie.customData as String,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(top = 1.dp, bottom = 2.dp),
                                    textAlign = TextAlign.Start,
                                    fontWeight = FontWeight.W300,
                                    fontSize = 12.sp,
                                    color = c.text,
                                )


                                Text(
                                    pie.subtitleTop!!,
                                    modifier = Modifier
                                        .padding(top = 1.dp, bottom = 2.dp),
                                    textAlign = TextAlign.Start,
                                    fontWeight = FontWeight.W300,
                                    fontSize = 12.sp,
                                    color = c.text,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
