package me.timeto.app.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.c
import me.timeto.app.toColor
import me.timeto.shared.misc.PieChart
import me.timeto.shared.limitMax
import java.lang.Math.PI

object WyouChart {

    private const val separatorDegrees = 8.0
    private const val ringRatio = 2.1
    private const val selectedOffset = 25.0

    /**
     * Remember that the block has a square shape
     */
    @Composable
    fun ChartUI(
        itemsData: List<PieChart.ItemData>,
        selectedId: String?,
        onIdSelected: (String?) -> Unit,
    ) {
        val totalValue = itemsData.sumOf { it.value }
        var lastDegree = 0.0
        val degreesForSlices = 360.0 - (separatorDegrees * itemsData.size)
        val separatorDegreesHalf = separatorDegrees / 2.0

        val slices = itemsData.map { itemData ->
            val degrees = degreesForSlices * itemData.value / totalValue
            val degreesFrom = lastDegree + separatorDegreesHalf
            val degreesTo = degreesFrom + degrees
            lastDegree = degreesTo + separatorDegreesHalf
            PieChart.SliceViewData(itemData.id, degreesFrom, degreesTo, itemData)
        }

        for (slice in slices) {

            val middleDegrees = (slice.degreesTo + slice.degreesFrom) / 2.0
            val selectedOffsetX = sinDegrees(middleDegrees) * selectedOffset
            val selectedOffsetY = cosDegrees(middleDegrees) * selectedOffset

            val isActive = slice.id == selectedId
            val animationX = animateDpAsState(if (isActive) selectedOffsetX.dp else 0.dp, spring(stiffness = Spring.StiffnessLow))
            val animationY = animateDpAsState(if (isActive) -selectedOffsetY.dp else 0.dp, spring(stiffness = Spring.StiffnessLow))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    /**
                     * Find the segment clicked.
                     */
                    .offset(x = animationX.value, y = animationY.value)
                    .pointerInput(itemsData) {
                        detectTapGestures(
                            onTap = { tapOffset ->

                                // Calculation of pressing coordinates relative to the center
                                val center = size.height.toFloat() / 2 // Since the block is square works and for the width of
                                val tapX = tapOffset.x - center
                                val tapY = (size.height.toFloat() - tapOffset.y - center)

                                val angleTmp = Math.toDegrees(
                                    kotlin.math
                                        .atan(tapY / tapX)
                                        .toDouble()
                                )
                                val angle = when {
                                    tapX > 0 && tapY > 0 -> 90 - angleTmp
                                    tapX > 0 && tapY < 0 -> 90 - angleTmp
                                    tapX < 0 && tapY < 0 -> 180 + 90 - angleTmp
                                    tapX < 0 && tapY > 0 -> 180 + 90 - angleTmp
                                    else -> throw Exception()
                                }

                                val clickableSlice = slices.first {
                                    angle > (it.degreesFrom - separatorDegreesHalf)
                                            &&
                                            angle < (it.degreesTo + separatorDegreesHalf)
                                }

                                onIdSelected(if (clickableSlice.id == selectedId) null else clickableSlice.id)
                            }
                        )
                    }

            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawPie(slice)
                }

                val (title, offsetK) = when {
                    (slice.degreesTo - slice.degreesFrom) > 20 -> slice.itemData.title to 3.59f
                    else -> slice.itemData.shortTitle to 3.9f
                }

                Text(
                    title,
                    modifier = Modifier
                        .width(70.dp) // Approximately
                        .align(Alignment.Center)
                        .offset(selectedOffsetX.dp * offsetK, -selectedOffsetY.dp * offsetK),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp,
                    color = c.white,
                )
            }
        }

        if (selectedId != null) {
            val item = itemsData.first { it.id == selectedId }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {

                if (item.subtitleTop != null)
                    Text(
                        item.subtitleTop!!,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W300,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp,
                        color = c.text,
                    )

                Text(
                    item.title,
                    modifier = Modifier
                        .width(80.dp) // Approximately
                        .padding(vertical = 2.dp)
                        .align(Alignment.CenterHorizontally),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = c.text,
                )

                if (item.subtitleBottom != null)
                    Text(
                        item.subtitleBottom!!,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W300,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp,
                        color = c.text,
                    )
            }
        }
    }

    private fun sinDegrees(degrees: Double) = kotlin.math.sin(degrees * PI / 180.0)

    private fun cosDegrees(degrees: Double) = kotlin.math.cos(degrees * PI / 180.0)

    private fun DrawScope.drawPie(data: PieChart.SliceViewData) {

        val s = size.height.limitMax(size.height)
        val center = s * 0.5f

        val maxBorder = 30.0 // Increases the size of the segment on each side by half of itself

        val oRadius = (s / 2) - (maxBorder / 2) // outer radius
        val iRadius = oRadius / ringRatio // inner radius

        val oc = 2.0 * PI * oRadius // outer C - circle length
        val ic = 2.0 * PI * iRadius // inner C - circle length

        val isa = (data.degreesTo - data.degreesFrom) * ic / 360.0 // inner segment arc
        val isMaxBorder = isa > maxBorder
        val border = if (isMaxBorder) maxBorder else isa
        val extraRadius = if (isMaxBorder) 0.0 else (maxBorder / 2.0) - (border / 2.0)

        val epsSafe = 0.00001 // Avoiding equal values, because the beginning may be after the end
        val oPd = ((border / 2) * 360) / oc - epsSafe // Outer padding
        val iPd = ((border / 2) * 360) / ic - epsSafe // Inner padding

        // Increasing the top arc for even indentation
        val extraOPd = separatorDegrees / 2.0 * (1.0 - (iRadius / oRadius))

        val oAStart = -90.0 + data.degreesFrom + oPd - extraOPd
        val oAEnd = -90.0 + data.degreesTo - oPd + extraOPd
        val iAStart = -90.0 + data.degreesFrom + iPd
        val iAEnd = -90.0 + data.degreesTo - iPd

        val nORadius = (oRadius + extraRadius).toFloat()
        val nIRadius = (iRadius + extraRadius).toFloat()
        val oRect = Rect(
            center - nORadius,
            center - nORadius,
            center + nORadius,
            center + nORadius,
        )
        val iRect = Rect(
            center - nIRadius,
            center - nIRadius,
            center + nIRadius,
            center + nIRadius,
        )

        val path = Path()
        path.arcTo(oRect, oAStart.toFloat(), (oAEnd - oAStart).toFloat(), false)
        path.arcTo(iRect, iAEnd.toFloat(), -(iAEnd - iAStart).toFloat(), false)
        path.close()

        drawPath(
            path,
            data.itemData.color.toColor(),
            style = Stroke(width = border.toFloat(), join = StrokeJoin.Round)
        )
        drawPath(path, data.itemData.color.toColor())
    }
}
