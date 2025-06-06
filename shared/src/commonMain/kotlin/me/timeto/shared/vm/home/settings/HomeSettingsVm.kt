package me.timeto.shared.vm.home.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.ColorRgba
import me.timeto.shared.Palette
import me.timeto.shared.vm.Vm
import kotlin.math.absoluteValue

typealias ButtonUi = HomeSettingsButtonUi

class HomeSettingsVm(
    private val spacing: Float,
    private val cellWidth: Float,
    private val rowHeight: Float,
) : Vm<HomeSettingsVm.State>() {

    companion object {
        const val cellsCount = 6
    }

    data class State(
        val buttonsData: ButtonsData,
        val update: Int = 0,
    )

    override val state: MutableStateFlow<State>

    init {

        val dataButtonsUiRaw: List<ButtonUi> = listOf(
            ButtonUi(rowIdx = 1, cellStartIdx = 0, cellsSize = 2, colorRgba = Palette.red.dark, spacing = spacing, cellWidth = cellWidth, rowHeight = rowHeight),
            ButtonUi(rowIdx = 1, cellStartIdx = 2, cellsSize = 3, colorRgba = Palette.blue.dark, spacing = spacing, cellWidth = cellWidth, rowHeight = rowHeight),
            ButtonUi(rowIdx = 2, cellStartIdx = 0, cellsSize = 2, colorRgba = Palette.purple.dark, spacing = spacing, cellWidth = cellWidth, rowHeight = rowHeight),
            ButtonUi(rowIdx = 3, cellStartIdx = 4, cellsSize = 2, colorRgba = Palette.cyan.dark, spacing = spacing, cellWidth = cellWidth, rowHeight = rowHeight),
        )

        val buttonsData = buildButtonsData(
            dataButtonsUiRaw = dataButtonsUiRaw,
            spacing = spacing,
            cellWidth = cellWidth,
            rowHeight = rowHeight,
        )

        state = MutableStateFlow(
            State(
                buttonsData = buttonsData,
            )
        )
    }

    fun getHoverButtonsUiOnDrag(
        buttonUi: ButtonUi,
        x: Float,
        y: Float,
    ): List<ButtonUi> {
        val buttonsData: ButtonsData = state.value.buttonsData

        val nearestButtonUi: ButtonUi = buttonsData.emptyButtonsUi.minBy { emptyButtonUi ->
            (emptyButtonUi.initX - x).absoluteValue + (emptyButtonUi.initY - y).absoluteValue
        }

        if ((nearestButtonUi.cellStartIdx + buttonUi.cellsSize) > cellsCount)
            return emptyList()

        val usedCellIds: List<Int> = buttonsData.dataButtonsUi
            .filter { it.id != buttonUi.id }
            .filter { it.rowIdx == nearestButtonUi.rowIdx }
            .map { it.cellStartIdx until (it.cellStartIdx + it.cellsSize) }
            .flatten()

        val hoverCellIds: IntRange =
            (nearestButtonUi.cellStartIdx until (nearestButtonUi.cellStartIdx + buttonUi.cellsSize))

        if (usedCellIds.intersect(hoverCellIds).isNotEmpty())
            return emptyList()

        val hoverButtonsUi = buttonsData.emptyButtonsUi
            .filter { it.rowIdx == nearestButtonUi.rowIdx }
            .filter { it.cellStartIdx in hoverCellIds }
            .map { it.copy(colorRgba = hoverButtonBgColorRgba) }

        return hoverButtonsUi
    }

    fun onButtonDragEnd(
        buttonUi: ButtonUi,
        x: Float,
        y: Float,
    ): Boolean {
        val hoverButtonsUi: List<ButtonUi> =
            getHoverButtonsUiOnDrag(buttonUi, x = x, y = y)
        if (hoverButtonsUi.isEmpty())
            return false

        val firstHoverButtonUi: ButtonUi =
            hoverButtonsUi.minBy { it.cellStartIdx }

        val newButtonsUi: List<ButtonUi> =
            state.value.buttonsData.dataButtonsUi.filter { it.id != buttonUi.id } +
            buttonUi.copy(
                rowIdx = firstHoverButtonUi.rowIdx,
                cellStartIdx = firstHoverButtonUi.cellStartIdx,
            )

        val buttonsData = buildButtonsData(
            dataButtonsUiRaw = newButtonsUi,
            spacing = spacing,
            cellWidth = cellWidth,
            rowHeight = rowHeight,
        )

        state.update {
            it.copy(
                buttonsData = buttonsData,
                update = it.update + 1,
            )
        }

        return true
    }

    fun getHoverButtonsUiOnResize(
        buttonUi: ButtonUi,
        left: Float,
        // todo
        right: Float,
    ): List<ButtonUi> {
        val rowIdx: Int = buttonUi.rowIdx
        val buttonsData: ButtonsData = state.value.buttonsData

        val usedCellIds: List<Int> = buttonsData.dataButtonsUi
            .filter { it.id != buttonUi.id }
            .filter { it.rowIdx == rowIdx }
            .map { it.cellStartIdx until (it.cellStartIdx + it.cellsSize) }
            .flatten()

        val nearestLeftEmptyButtonUi: ButtonUi = buttonsData.emptyButtonsUi.minBy { emptyButtonUi ->
            (emptyButtonUi.initX - (buttonUi.initX - left)).absoluteValue
        }

        val hoverCellIds: IntRange =
            // todo until nearestRightEmptyButtonUi
            (nearestLeftEmptyButtonUi.cellStartIdx until (buttonUi.cellStartIdx + buttonUi.cellsSize))

        if (usedCellIds.intersect(hoverCellIds).isNotEmpty())
            return emptyList()

        val hoverButtonsUi = buttonsData.emptyButtonsUi
            .filter { it.rowIdx == rowIdx }
            .filter { it.cellStartIdx in hoverCellIds }
            .map { it.copy(colorRgba = hoverButtonBgColorRgba) }

        return hoverButtonsUi
    }

    fun onButtonResizeEnd(
        buttonUi: ButtonUi,
        left: Float,
        right: Float,
    ): Boolean {
        val hoverButtonsUi: List<ButtonUi> =
            getHoverButtonsUiOnResize(buttonUi, left = left, right = right)
        if (hoverButtonsUi.isEmpty())
            return false

        val firstHoverCellIdx: Int =
            hoverButtonsUi.minOf { it.cellStartIdx }

        val lastHoverButtonUi: Int =
            hoverButtonsUi.maxOf { it.cellStartIdx }

        val newButtonsUi: List<ButtonUi> =
            state.value.buttonsData.dataButtonsUi.filter { it.id != buttonUi.id } +
            buttonUi.copy(
                cellStartIdx = firstHoverCellIdx,
                cellsSize = lastHoverButtonUi - firstHoverCellIdx + 1,
            )

        val buttonsData = buildButtonsData(
            dataButtonsUiRaw = newButtonsUi,
            spacing = spacing,
            cellWidth = cellWidth,
            rowHeight = rowHeight,
        )

        state.update {
            it.copy(
                buttonsData = buttonsData,
                update = it.update + 1,
            )
        }

        return true
    }

    ///

    data class ButtonsData(
        val rowsCount: Int,
        val emptyButtonsUi: List<ButtonUi>,
        val dataButtonsUi: List<ButtonUi>,
    )
}

private val hoverButtonBgColorRgba: ColorRgba = Palette.gray2.dark
private val emptyButtonBgColorRgba: ColorRgba = Palette.gray5.dark
private val blackColorRgba = ColorRgba(0, 0, 0, 255)

private fun buildEmptyButtonsUi(
    rowsCount: Int,
    spacing: Float,
    cellWidth: Float,
    rowHeight: Float,
): List<ButtonUi> =
    (0 until rowsCount)
        .map { rowIdx ->
            (0 until HomeSettingsVm.cellsCount)
                .map { cellIdx ->
                    ButtonUi(
                        rowIdx = rowIdx,
                        cellStartIdx = cellIdx,
                        cellsSize = 1,
                        colorRgba = if ((rowIdx % 2) == 0) blackColorRgba else emptyButtonBgColorRgba,
                        spacing = spacing,
                        cellWidth = cellWidth,
                        rowHeight = rowHeight,
                    )
                }
        }
        .flatten()


private fun buildButtonsData(
    dataButtonsUiRaw: List<ButtonUi>,
    spacing: Float,
    cellWidth: Float,
    rowHeight: Float,
): HomeSettingsVm.ButtonsData {
    val dataButtonsUiRawRows: List<List<ButtonUi>> = dataButtonsUiRaw
        .groupBy { it.rowIdx }.toList().sortedBy { it.first }.map { it.second }

    val dataButtonsUiForGrid = dataButtonsUiRawRows
        .mapIndexed { rowIdx, buttonsUi ->
            buttonsUi.map { it.copy(rowIdx = (rowIdx * 2 + 1)) }
        }
        .flatten()

    val rowsCount: Int = dataButtonsUiRawRows.size * 2 + 1

    return HomeSettingsVm.ButtonsData(
        rowsCount = rowsCount,
        emptyButtonsUi = buildEmptyButtonsUi(
            rowsCount = rowsCount,
            spacing = spacing,
            cellWidth = cellWidth,
            rowHeight = rowHeight,
        ),
        dataButtonsUi = dataButtonsUiForGrid,
    )
}
