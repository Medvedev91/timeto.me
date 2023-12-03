package me.timeto.shared

fun zlog(message: Any?) = println(";; ${message.toString().replace("\n", "\n;; ")}")
