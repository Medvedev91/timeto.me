package me.timeto.shared.ui

import me.timeto.shared.launchExIo

fun <T> List<T>.moveIos(
    from: Int,
    to: Int,
    action: suspend (List<T>) -> Unit,
) {
    val oldList: List<T> = this
    val newList: MutableList<T> = oldList.toMutableList()
    val fromItem: T = oldList[from]
    newList.removeAt(from)
    newList.add(to, fromItem)
    launchExIo {
        action(newList)
    }
}
