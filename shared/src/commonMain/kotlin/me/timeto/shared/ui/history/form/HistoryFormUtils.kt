package me.timeto.shared.ui.history.form

import me.timeto.shared.UnixTime
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures
import me.timeto.shared.ui.DialogsManager

object HistoryFormUtils {

    fun deleteIntervalUi(
        intervalDb: IntervalDb,
        dialogsManager: DialogsManager,
        onSuccess: suspend () -> Unit,
    ) {
        val activityDb: ActivityDb = intervalDb.selectActivityDbCached()
        val intervalNote: String? = intervalDb.note?.takeIf { it.isNotBlank() }
        val note: String = (intervalNote ?: activityDb.name)
            .textFeatures()
            .textNoFeatures
        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"$note\"",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    intervalDb.delete()
                    onSuccess()
                }
            },
        )
    }

    fun prepTimeNote(
        time: Int,
        withToday: Boolean,
    ): String {
        val today: Int = UnixTime().localDay
        val unixTime = UnixTime(time)
        return if (today == unixTime.localDay) {
            (if (withToday) "Today " else "") +
            unixTime.getStringByComponents(
                listOf(
                    UnixTime.StringComponent.hhmm24,
                )
            )
        } else {
            unixTime.getStringByComponents(
                listOf(
                    UnixTime.StringComponent.dayOfMonth,
                    UnixTime.StringComponent.space,
                    UnixTime.StringComponent.month3,
                    UnixTime.StringComponent.comma,
                    UnixTime.StringComponent.space,
                    UnixTime.StringComponent.dayOfWeek3,
                    UnixTime.StringComponent.space,
                    UnixTime.StringComponent.hhmm24,
                )
            )
        }
    }
}
