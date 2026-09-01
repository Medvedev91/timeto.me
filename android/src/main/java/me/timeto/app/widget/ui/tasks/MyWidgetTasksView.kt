package me.timeto.app.widget.ui.tasks

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.lazy.LazyColumn
import me.timeto.shared.vm.home.tasks.HomeTasksItemUi

@Composable
fun MyWidgetTasksView(
    homeTasksItemsUi: List<HomeTasksItemUi>,
    glanceModifier: GlanceModifier,
) {
    LazyColumn(
        modifier = glanceModifier,
    ) {
        homeTasksItemsUi.reversed().forEach { itemUi ->
            item {
                when (itemUi) {
                    is HomeTasksItemUi.HomeTaskUi -> MyWidgetTaskView(
                        homeTaskUi = itemUi,
                    )
                    is HomeTasksItemUi.HomeTomorrowItemUi -> {
                        // todo
                    }
                }
            }
        }
    }
}
