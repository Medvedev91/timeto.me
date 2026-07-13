package me.timeto.shared.vm.home.tasks

import me.timeto.shared.TaskFolderUi
import me.timeto.shared.TaskUi
import me.timeto.shared.TextFeatures
import me.timeto.shared.UnixTime
import me.timeto.shared.vm.task_form.TaskFormStrategy

sealed class HomeTasksItemUi(
    val id: String,
) {

    data class HomeTaskUi(
        val taskUi: TaskUi,
        val allTaskFoldersUi: List<TaskFolderUi>,
    ) : HomeTasksItemUi(id = "HomeTaskUi_${taskUi.taskDb.id}") {

        val text: String =
            taskUi.tf.textUi()

        val staTaskFoldersUi: List<HomeTaskStaTaskFolderUi> = allTaskFoldersUi
            .homeTasksFoldersSorted()
            .map { HomeTaskStaTaskFolderUi(taskUi, it) }

        val timeUi: TimeUi? = taskUi.tf.calcTimeData()?.let { timeData ->
            TimeUi(
                timeData = timeData,
            )
        }

        val editStrategy = TaskFormStrategy.EditTask(
            taskDb = taskUi.taskDb,
        )

        class TimeUi(
            val timeData: TextFeatures.TimeData,
        ) {
            val text: String = timeData.timeText()
            val note: String = timeData.timeLeftText()
            val status: TextFeatures.TimeData.STATUS = timeData.status
        }
    }

    class HomeTomorrowItemUi(
        val tf: TextFeatures,
        val type: TomorrowType,
        val listId: Int,
    ) : HomeTasksItemUi(id = "HomeTomorrowTaskUi_$listId") {

        val text: String =
            tf.textUi()

        val timeUi: TomorrowTimeUi? = tf.calcTimeData()?.let { timeData ->
            TomorrowTimeUi(
                timeData = timeData,
            )
        }

        class TomorrowTimeUi(
            val timeData: TextFeatures.TimeData,
        ) {
            val text: String = timeData.unixTime.getStringByComponents(
                UnixTime.StringComponent.dayOfMonth,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.month3,
                UnixTime.StringComponent.comma,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.hhmm24,
            )
        }

        enum class TomorrowType {
            repeating, calendar,
        }
    }
}
