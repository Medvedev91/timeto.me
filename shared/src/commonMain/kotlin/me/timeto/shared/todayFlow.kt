package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow

val todayFlow = MutableStateFlow(UnixTime().localDay)
