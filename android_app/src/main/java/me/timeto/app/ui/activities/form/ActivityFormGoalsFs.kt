package me.timeto.app.ui.activities.form

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ui.activities.form.ActivityFormGoalsVm
import me.timeto.shared.ui.goals.form.GoalFormData

@Composable
fun ActivityFormGoalsFs(
    initGoalFormsData: List<GoalFormData>,
    onDone: (List<GoalFormData>) -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ActivityFormGoalsVm(
            initGoalFormsData = initGoalFormsData,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = "Goals",
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    onDone(state.goalFormsData)
                    navigationLayer.close()
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )
    }
}
