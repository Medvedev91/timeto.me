package me.timeto.shared

import me.timeto.shared.db.Goal2Db
import me.timeto.shared.db.dbIo
import me.timeto.shared.vm.home.buttons.homeButtonsCellsCount

data class HomeButtonSort(
    val rowIdx: Int,
    val cellIdx: Int,
    val size: Int,
) {

    val string = "$rowIdx:$cellIdx:$size"

    companion object {

        const val visibleRows = 8

        fun parseOrNull(string: String): HomeButtonSort? {
            val raw: List<Int> =
                string.split(':').mapNotNull { it.toIntOrNull() }
            if (raw.size != 3)
                return null
            return HomeButtonSort(
                rowIdx = raw[0],
                cellIdx = raw[1],
                size = raw[2],
            )
        }

        suspend fun findNextPosition(isHidden: Boolean, barSize: Int): HomeButtonSort = dbIo {
            findNextPositionSync(isHidden = isHidden, barSize = barSize)
        }

        fun findNextPositionSync(isHidden: Boolean, barSize: Int): HomeButtonSort {
            val filledCells: Set<String> = Goal2Db.selectAllSync().mapNotNull { goal2Db ->
                val sort = parseOrNull(goal2Db.home_button_sort) ?: return@mapNotNull null
                (sort.cellIdx until (sort.cellIdx + sort.size)).map { cellIdx ->
                    "${sort.rowIdx}:$cellIdx"
                }
            }.flatten().toSet()
            var rowIdx: Int =
                if (isHidden) visibleRows else 0
            while (true) {
                (0..(homeButtonsCellsCount - barSize)).forEach { cellStartIdx ->
                    val neededCells: Set<String> =
                        (0 until barSize).map { "$rowIdx:${cellStartIdx + it}" }.toSet()
                    if (filledCells.intersect(neededCells).isEmpty())
                        return HomeButtonSort(rowIdx, cellStartIdx, barSize)
                }
                rowIdx++
            }
        }
    }
}
