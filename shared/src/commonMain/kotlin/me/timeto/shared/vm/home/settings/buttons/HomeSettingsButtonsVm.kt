package me.timeto.shared.vm.home.settings.buttons

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.Palette
import me.timeto.shared.launchExIo
import me.timeto.shared.vm.Vm
import me.timeto.shared.vm.home.buttons.homeButtonsCellsCount
import kotlin.math.absoluteValue

typealias ButtonUi = HomeSettingsButtonUi

class HomeSettingsButtonsVm(
    private val spacing: Float,
    private val cellWidth: Float,
    private val rowHeight: Float,
) : Vm<HomeSettingsButtonsVm.State>() {

    data class State(
        val buttonsData: ButtonsData,
        val update: Int = 0,
    ) {
        val title = "Home Settings"
    }

    override val state: MutableStateFlow<State>

    init {

        val dataButtonsUiRaw: List<ButtonUi> = Cache.goalsDb.map { goalDb ->
            ButtonUi(
                type = HomeSettingsButtonType.Goal(goalDb),
                sort = HomeButtonSort.parseOrDefault(goalDb.home_button_sort),
                colorRgba = goalDb.getActivityDbCached().colorRgba,
                spacing = spacing,
                cellWidth = cellWidth,
                rowHeight = rowHeight,
            )
        }

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

    fun save() {
        launchExIo {
            state.value.buttonsData.dataButtonsUi.forEach { buttonUi ->
                when (val type = buttonUi.type) {
                    is HomeSettingsButtonType.Goal ->
                        type.goalDb.updateHomeButtonSort(buttonUi.sort)
                    is HomeSettingsButtonType.Empty -> {
                    }
                }
            }
        }
    }

    fun getHoverButtonsUiOnDrag(
        buttonUi: ButtonUi,
        x: Float,
        y: Float,
    ): List<ButtonUi> {
        val buttonsData: ButtonsData = state.value.buttonsData

        val nearestButtonUi: ButtonUi = buttonsData.emptyButtonsUi.minBy { emptyButtonUi ->
            (emptyButtonUi.offsetX - x).absoluteValue + (emptyButtonUi.offsetY - y).absoluteValue
        }

        if ((nearestButtonUi.sort.cellIdx + buttonUi.sort.size) > homeButtonsCellsCount)
            return emptyList()

        val usedCellIds: List<Int> = buttonsData.dataButtonsUi
            .filter { it.id != buttonUi.id }
            .filter { it.sort.rowIdx == nearestButtonUi.sort.rowIdx }
            .map { it.sort.cellIdx until (it.sort.cellIdx + it.sort.size) }
            .flatten()

        val hoverCellIds: IntRange =
            (nearestButtonUi.sort.cellIdx until (nearestButtonUi.sort.cellIdx + buttonUi.sort.size))

        if (usedCellIds.intersect(hoverCellIds).isNotEmpty())
            return emptyList()

        val hoverButtonsUi = buttonsData.emptyButtonsUi
            .filter { it.sort.rowIdx == nearestButtonUi.sort.rowIdx }
            .filter { it.sort.cellIdx in hoverCellIds }
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
            hoverButtonsUi.minBy { it.sort.cellIdx }

        val newButtonsUi: List<ButtonUi> =
            state.value.buttonsData.dataButtonsUi.filter { it.id != buttonUi.id } +
            buttonUi.copy(
                sort = buttonUi.sort.copy(
                    rowIdx = firstHoverButtonUi.sort.rowIdx,
                    cellIdx = firstHoverButtonUi.sort.cellIdx,
                )
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
        right: Float,
    ): List<ButtonUi> {
        val rowIdx: Int = buttonUi.sort.rowIdx
        val buttonsData: ButtonsData = state.value.buttonsData

        val usedCellIds: List<Int> = buttonsData.dataButtonsUi
            .filter { it.id != buttonUi.id }
            .filter { it.sort.rowIdx == rowIdx }
            .map { it.sort.cellIdx until (it.sort.cellIdx + it.sort.size) }
            .flatten()

        val nearestLeftEmptyButtonUi: ButtonUi = buttonsData.emptyButtonsUi.minBy { emptyButtonUi ->
            (emptyButtonUi.offsetX - (buttonUi.offsetX - left)).absoluteValue
        }

        val nearestRightEmptyButtonUi: ButtonUi = buttonsData.emptyButtonsUi.minBy { emptyButtonUi ->
            (emptyButtonUi.offsetX - (buttonUi.offsetX + buttonUi.fullWidth - buttonUi.cellWidth + right)).absoluteValue
        }

        val hoverCellIds: IntRange =
            (nearestLeftEmptyButtonUi.sort.cellIdx..nearestRightEmptyButtonUi.sort.cellIdx)

        if (usedCellIds.intersect(hoverCellIds).isNotEmpty())
            return emptyList()

        val hoverButtonsUi = buttonsData.emptyButtonsUi
            .filter { it.sort.rowIdx == rowIdx }
            .filter { it.sort.cellIdx in hoverCellIds }
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
            hoverButtonsUi.minOf { it.sort.cellIdx }

        val lastHoverButtonUi: Int =
            hoverButtonsUi.maxOf { it.sort.cellIdx }

        val newButtonsUi: List<ButtonUi> =
            state.value.buttonsData.dataButtonsUi.filter { it.id != buttonUi.id } +
            buttonUi.copy(
                sort = buttonUi.sort.copy(
                    cellIdx = firstHoverCellIdx,
                    size = lastHoverButtonUi - firstHoverCellIdx + 1,
                )
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
            (0 until homeButtonsCellsCount)
                .map { cellIdx ->
                    ButtonUi(
                        type = HomeSettingsButtonType.Empty,
                        sort = HomeButtonSort(
                            rowIdx = rowIdx,
                            cellIdx = cellIdx,
                            size = 1,
                        ),
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
): HomeSettingsButtonsVm.ButtonsData {
    val buttonsUiWithInvalidPosition: MutableList<ButtonUi> =
        mutableListOf()
    val dataButtonsUiRawRows: List<List<ButtonUi>> = dataButtonsUiRaw
        .groupBy { it.sort.rowIdx }.toList().sortedBy { it.first }.map { it.second }
        .map { row ->
            val usedCellIds: MutableSet<Int> = mutableSetOf()
            row.mapNotNull { buttonUi ->
                val cellIds: IntRange =
                    buttonUi.sort.cellIdx until (buttonUi.sort.cellIdx + buttonUi.sort.size)
                if (usedCellIds.intersect(cellIds).isNotEmpty()) {
                    buttonsUiWithInvalidPosition.add(buttonUi)
                    return@mapNotNull null
                }
                usedCellIds.addAll(cellIds)
                buttonUi
            }
        }
        .plus(
            buttonsUiWithInvalidPosition.map { buttonUi ->
                listOf(
                    buttonUi.copy(
                        sort = buttonUi.sort.copy(
                            cellIdx = 0,
                            size = 3,
                        ),
                    )
                )
            }
        )

    val dataButtonsUiForGrid = dataButtonsUiRawRows
        .mapIndexed { rowIdx, buttonsUi ->
            buttonsUi.map {
                it.copy(
                    sort = it.sort.copy(
                        rowIdx = (rowIdx * 2 + 1),
                    ),
                )
            }
        }
        .flatten()

    val rowsCount: Int = dataButtonsUiRawRows.size * 2 + 1

    return HomeSettingsButtonsVm.ButtonsData(
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
