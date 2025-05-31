package me.timeto.shared

/**
 * Do use only elements from the array, otherwise Exception
 */
fun <T> List<T>.getNextOrNull(item: T): T? {
    val index = indexOf(item)
    if (index < 0)
        throw Exception() // todo report
    return if (index + 1 == size) null else get(index + 1)
}
