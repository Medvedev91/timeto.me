package me.timeto.shared

// Do not use "\\s+" because it removes line breaks.
private val duplicateSpacesRegex = " +".toRegex()

fun String.removeDuplicateSpaces(): String =
    this.replace(duplicateSpacesRegex, " ")

