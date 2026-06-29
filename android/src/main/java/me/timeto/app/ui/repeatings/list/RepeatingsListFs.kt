package me.timeto.app.ui.repeatings.list

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.timeto.app.MainActivity
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.Screen
import me.timeto.app.ui.c
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.repeatings.form.RepeatingFormFs
import me.timeto.app.ui.squircleShape
import me.timeto.shared.vm.repeatings.list.RepeatingsListVm

@Composable
fun RepeatingsListFs() {

    val navigationFs = LocalNavigationFs.current
    val mainActivity = LocalActivity.current as MainActivity

    val (_, state) = rememberVm {
        RepeatingsListVm()
    }

    Screen {

        LazyColumn(
            reverseLayout = true,
            modifier = Modifier
                .padding(top = mainActivity.statusBarHeightDp)
                .navigationBarsPadding()
                .fillMaxHeight(),
        ) {

            item {

                Text(
                    text = "New Repeating Task",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = H_PADDING)
                        .padding(top = 20.dp)
                        .clip(squircleShape)
                        .background(c.blue)
                        .clickable {
                            navigationFs.push {
                                RepeatingFormFs(
                                    initRepeatingDb = null,
                                )
                            }
                        }
                        .padding(vertical = 10.dp),
                    color = c.white,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }

            val repeatingsUi = state.repeatingsUi
            repeatingsUi.forEachIndexed { idx, repeatingUi ->
                item {
                    RepeatingsListItemView(
                        repeatingUi = repeatingUi,
                        // Remember that the list is reversed
                        withTopDivider = (idx != repeatingsUi.size - 1),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = H_PADDING_HALF),
                    )
                }
            }
        }
    }
}
