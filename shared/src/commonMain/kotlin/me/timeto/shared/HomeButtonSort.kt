package me.timeto.shared

data class HomeButtonSort(
    val rowIdx: Int,
    val cellIdx: Int,
    val size: Int,
) {

    val string = "$rowIdx:$cellIdx:$size"

    companion object {

        fun parseOrDefault(
            string: String,
        ): HomeButtonSort {
            val raw: List<Int> =
                string.split(':').mapNotNull { it.toIntOrNull() }
            if (raw.size != 3)
                return defaultSort
            return HomeButtonSort(
                rowIdx = raw[0],
                cellIdx = raw[1],
                size = raw[2],
            )
        }
    }
}

private val defaultSort = HomeButtonSort(
    rowIdx = 0,
    cellIdx = 0,
    size = 3,
)
