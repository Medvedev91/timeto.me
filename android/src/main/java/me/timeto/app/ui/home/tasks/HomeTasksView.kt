package me.timeto.app.ui.home.tasks

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.MainActivity
import me.timeto.app.ui.c
import me.timeto.app.ui.header.Header__titleFontSize
import me.timeto.app.ui.header.Header__titleFontWeight
import me.timeto.app.ui.home.HomeScreen__hPadding
import me.timeto.shared.vm.home.HomeVm
import me.timeto.shared.vm.home.tasks.HomeTasksItemUi

@Composable
fun HomeTasksView(
    homeState: HomeVm.State,
    modifier: Modifier,
) {

    val scrollState = rememberLazyListState()
    val mainActivity = LocalActivity.current as MainActivity

    if (!homeState.taskFolderUi.taskFolderDb.isToday) {
        Text(
            text = homeState.taskFolderUi.taskFolderDb.name,
            modifier = Modifier
                .padding(
                    top = mainActivity.statusBarHeightDp,
                    start = HomeScreen__hPadding,
                    bottom = 8.dp,
                ),
            fontSize = Header__titleFontSize,
            fontWeight = Header__titleFontWeight,
            color = c.text,
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        state = scrollState,
        reverseLayout = true,
    ) {

        items(
            items = homeState.homeTasksItemsUi,
            key = { it.id },
        ) { itemUi ->
            when (itemUi) {
                is HomeTasksItemUi.HomeTaskUi -> HomeTaskView(
                    homeTaskUi = itemUi,
                    homeState = homeState,
                )
                is HomeTasksItemUi.HomeTomorrowItemUi -> HomeTasksTomorrowView(
                    tomorrowUi = itemUi,
                )
            }
        }
    }
}
