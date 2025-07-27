package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import kotlin.math.absoluteValue

data class LiveActivity(
    val intervalDb: IntervalDb,
    val activityDb: ActivityDb,
    val iosDynamicIslandTimer: String,
) {

    val dynamicIslandTitle: String =
        (intervalDb.note ?: activityDb.name).textFeatures().textNoFeatures

    ///

    companion object {

        val flow = MutableStateFlow<LiveActivity?>(null)

        suspend fun update(intervalDb: IntervalDb) {
            val activityDb = intervalDb.selectActivityDb()
            val (h, m, s) = time().toHms()
            flow.emit(
                LiveActivity(
                    intervalDb = intervalDb,
                    activityDb = activityDb,
                    iosDynamicIslandTimer = "$m",
                )
            )
        }

        fun buildIosDynamicIslandData(
            timeStart: Int,
            timer: Int,
        ): DynamicIslandUi {
            val seconds: Int = (timeStart + timer) - time()
            val abs: Int = seconds.absoluteValue
            val (_, _, s) = abs.toHms()
            reportApi(";; $seconds $abs ${abs / 60}")
            return DynamicIslandUi(
                text = when {
                    abs < 60 -> "${abs}s"
                    abs < 3_600 -> "${abs / 60}"
                    else -> {
                        val (h, m, _) = abs.toHms()
                        when {
                            h == 0 -> "$m"
                            else -> "$h:${m.toString().padStart(2, '0')}"
                        }
                    }
                },
                tmpText = "${s}",
                color = when {
                    seconds == 0 -> ColorEnum.green
                    seconds > 0 -> ColorEnum.text
                    else -> ColorEnum.red
                },
            )
        }
    }

    data class DynamicIslandUi(
        val text: String,
        val tmpText: String,
        val color: ColorEnum,
    )
}
