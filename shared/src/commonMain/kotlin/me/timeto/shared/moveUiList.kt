package me.timeto.shared

fun <T> List<T>.moveUiListAndroid(
    fromIdx: Int,
    toIdx: Int,
    action: suspend (List<T>) -> Unit,
) {
    val oldList: List<T> = this
    val fromItem: T = oldList[fromIdx]
    val toItem: T = oldList[toIdx]
    val newItems: MutableList<T> = this.toMutableList()
    newItems[fromIdx] = toItem
    newItems[toIdx] = fromItem
    launchExIo {
        action(newItems)
    }
}

fun <T> List<T>.moveUiListIos(
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
