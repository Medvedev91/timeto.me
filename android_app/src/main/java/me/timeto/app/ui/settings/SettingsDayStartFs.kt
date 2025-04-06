package me.timeto.app.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.FormPlainButtonSelection
import me.timeto.app.ui.form.FormPlainPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ui.settings.SettingsVm

@Composable
fun SettingsDayStartFs(
    vm: SettingsVm,
    state: SettingsVm.State,
) {

    val navigationLayer = LocalNavigationLayer.current

    Screen(
        modifier = Modifier
            .navigationBarsPadding(),
    ) {

        val scrollState = rememberLazyListState()

        Header(
            title = "Day Start",
            scrollState = scrollState,
            actionButton = null,
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
        ) {

            item {
                FormPlainPaddingTop()
            }

            val dayStartListItems = state.dayStartListItems
            dayStartListItems.forEach { item ->
                item {
                    FormPlainButtonSelection(
                        title = item.note,
                        isSelected = item.seconds == state.dayStartSeconds,
                        isFirst = dayStartListItems.first() == item,
                        modifier = Modifier,
                        onClick = {
                            vm.setDayStartOffsetSeconds(item.seconds)
                            navigationLayer.close()
                        },
                    )
                }
            }
        }
    }
}
