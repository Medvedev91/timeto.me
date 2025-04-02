package me.timeto.app.ui.activities

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ui.activities.ActivitiesFormVm

@Composable
fun ActivitiesFormFs() {

    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ActivitiesFormVm()
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    navigationLayer.close()
                },
            ),
            cancelButton = null,
        )
    }
}
