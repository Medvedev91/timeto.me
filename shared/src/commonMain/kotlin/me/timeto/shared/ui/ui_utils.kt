package me.timeto.shared.ui

import me.timeto.shared.launchExIo

fun <T> List<T>.moveIos(
    fromIdx: Int,
    toIdx: Int,
    action: suspend (List<T>) -> Unit,
) {
    val oldList: List<T> = this
    val newList: MutableList<T> = oldList.toMutableList()
    val fromItem: T = oldList[fromIdx]
    newList.removeAt(fromIdx)
    newList.add(toIdx, fromItem)
    launchExIo {
        action(newList)
    }
}
