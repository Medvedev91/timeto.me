package me.timeto.app.ui.activities.timer

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.activities.ActivitiesView
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ui.activities.timer.ActivitiesTimerVm
import me.timeto.shared.ui.activities.timer.ActivityTimerStrategy

@Composable
fun ActivitiesTimerFs(
    strategy: ActivityTimerStrategy,
) {

    val navigationLayer = LocalNavigationLayer.current

    val (_, state) = rememberVm {
        ActivitiesTimerVm()
    }

    Screen(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) {
        ActivitiesView(
            timerStrategy = strategy,
            modifier = Modifier
                .weight(1f),
        )
    }

    val initIntervalId: Int = remember { state.lastIntervalId }
    val lastIntervalId: Int = state.lastIntervalId
    LaunchedEffect(lastIntervalId) {
        if (lastIntervalId != initIntervalId)
            navigationLayer.close()
    }
}
