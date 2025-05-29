package me.timeto.shared.extensions

fun Int.limitMin(value: Int): Int =
    if (this < value) value else this

fun Int.limitMax(value: Int): Int =
    if (this > value) value else this

fun Int.limitMinMax(min: Int, max: Int): Int =
    this.limitMin(min).limitMax(max)
