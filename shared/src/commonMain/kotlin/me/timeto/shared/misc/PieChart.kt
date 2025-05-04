package me.timeto.shared.misc

import me.timeto.shared.ColorRgba

object PieChart {

    class ItemData(
        val id: String,
        val value: Double,
        val color: ColorRgba,
        val title: String,
        val shortTitle: String,
        val subtitleTop: String? = null,
        val subtitleBottom: String? = null,
        val customData: Any? = null,
    )

    class SliceViewData(
        val id: String,
        val degreesFrom: Double,
        val degreesTo: Double,
        val itemData: ItemData,
    )
}
