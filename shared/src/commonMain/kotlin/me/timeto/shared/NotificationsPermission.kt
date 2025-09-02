package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow

enum class NotificationsPermission {

    notAsked, denied, rationale, granted;

    companion object {

        val flow = MutableStateFlow<NotificationsPermission?>(null)

        fun emit(permission: NotificationsPermission) {
            launchExIo {
                flow.emit(permission)
            }
        }
    }
}
