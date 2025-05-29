package me.timeto.shared

class Wheel<T>(
    private val items: List<T>,
) {

    private var lastIndex = -1

    fun next(): T {
        var nextIndex = lastIndex + 1
        if (nextIndex >= items.size)
            nextIndex = 0
        lastIndex = nextIndex
        return items[nextIndex]
    }
}
