package me.timeto.app.ui.activities

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.timeto.app.ui.HStack
import me.timeto.app.ui.c
import me.timeto.app.ui.squircleShape
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.history.HistoryFs
import me.timeto.app.ui.summary.SummaryFs
import me.timeto.app.ui.navigation.LocalNavigationFs

@Composable
fun ActivitiesScreen(
    onClose: () -> Unit,
) {
    BackHandler {
        onClose()
    }

    Screen {
        HistoryFs()
        BottomMenu(
            onClose = onClose,
        )
    }
}

///

@Composable
private fun BottomMenu(
    onClose: () -> Unit,
) {

    val scope = rememberCoroutineScope()
    val navigationFs = LocalNavigationFs.current

    HStack(
        modifier = Modifier
            .height(42.dp)
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        HStack(
            modifier = Modifier
                .clip(squircleShape)
                .clickable {
                    navigationFs.push {
                        SummaryFs()
                    }
                    scope.launch {
                        delay(1_000)
                        onClose()
                    }
                }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = "Summary",
                color = c.blue,
            )
        }

        SpacerW1()
    }
}
