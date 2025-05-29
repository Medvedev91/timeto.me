package me.timeto.shared

import me.timeto.shared.ui.UiException

data class ColorRgba(
    val r: Int, val g: Int,
    val b: Int, val a: Int = 255,
) {

    companion object {

        fun fromRgbaStringEx(rgbaString: String): ColorRgba =
            rgbaString.split(',').map { it.toInt() }.let {
                when (it.size) {
                    3 -> ColorRgba(it[0], it[1], it[2])
                    4 -> ColorRgba(it[0], it[1], it[2], it[3])
                    else -> {
                        reportApi("ColorRgba.fromRgbaString($rgbaString) invalid")
                        throw UiException("Invalid color")
                    }
                }
            }
    }

    fun toRgbaString(): String =
        "$r,$g,$b,$a"
}
