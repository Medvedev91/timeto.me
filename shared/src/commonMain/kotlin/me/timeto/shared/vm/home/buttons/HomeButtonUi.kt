package me.timeto.shared.vm.home.buttons

import me.timeto.shared.HomeButtonSort
import kotlin.Float

data class HomeButtonUi(
    val type: HomeButtonType,
    val sort: HomeButtonSort,
    val width: Float,
    val offsetX: Float,
    val offsetY: Float,
) {

    companion object {

        fun build(
            type: HomeButtonType,
            sort: HomeButtonSort,
            fullWidth: Float,
            rowHeight: Float,
            spacing: Float,
        ): HomeButtonUi {
            val cellWidth: Float =
                (fullWidth - ((homeButtonsCellsCount - 1) * spacing)) / homeButtonsCellsCount
            return HomeButtonUi(
                type = type,
                sort = sort,
                width = (cellWidth * sort.size) + ((sort.size - 1) * spacing),
                offsetX = (sort.cellIdx * cellWidth) + (sort.cellIdx * spacing),
                offsetY = sort.rowIdx * rowHeight,
            )
        }
    }

    fun recalculateUi(): HomeButtonUi {
        when (type) {
            is HomeButtonType.Goal -> {
                val newType: HomeButtonType.Goal =
                    type.recalculateUiIfNeeded() ?: return this
                return this.copy(type = newType)
            }
        }
    }
}
