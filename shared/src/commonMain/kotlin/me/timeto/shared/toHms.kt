package me.timeto.shared

fun Int.toHms(
    roundToNextMinute: Boolean = false
): List<Int> {

    val time = if (!roundToNextMinute) this
    else {
        val rmd = this % 60
        if (rmd == 0) this else (this + (60 - rmd))
    }

    var secondsLeft = time
    val h = secondsLeft / 3600
    secondsLeft -= h * 3600
    val m = secondsLeft / 60
    secondsLeft -= m * 60
    return listOf(h, m, secondsLeft)
}
