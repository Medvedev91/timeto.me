package me.timeto.shared.vm.home.settings

import me.timeto.shared.ColorRgba
import kotlin.math.absoluteValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class HomeSettingsButtonUi(
    val rowIdx: Int,
    val cellStartIdx: Int,
    val cellsSize: Int,
    val colorRgba: ColorRgba,
    val spacing: Float,
    val cellWidth: Float,
    val rowHeight: Float,
) {

    @OptIn(ExperimentalUuidApi::class)
    val id: String = Uuid.random().toString()

    val initX: Float =
        (cellStartIdx.toFloat() * cellWidth) + (cellStartIdx.toFloat() * spacing)
    val initY: Float =
        rowIdx.toFloat() * rowHeight

    val fullWidth: Float =
        ((cellWidth * cellsSize) + ((cellsSize - 1).toFloat() * spacing)).absoluteValue

    val resizeLeftMinOffset: Float =
        -((cellWidth * (cellsSize - 1)) + (spacing * (cellsSize - 1)))

    val resizeLeftMaxOffset: Float =
        (cellWidth * cellStartIdx) + (spacing * cellStartIdx)
}
