package me.timeto.shared

fun Float.limitMin(value: Float): Float =
    if (this < value) value else this

fun Float.limitMax(value: Float): Float =
    if (this > value) value else this

fun Float.limitMinMax(min: Float, max: Float): Float =
    this.limitMin(min).limitMax(max)
