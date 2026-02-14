package me.timeto.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.timeto.app.R
import me.timeto.app.ui.HStack
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.checklists.form.ChecklistFormItemsFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.roundedShape
import me.timeto.shared.vm.home.HomeVm

@Composable
fun HomeChecklistHintView(
    hintUi: HomeVm.ChecklistHintUi,
) {

    val navigation = LocalNavigationFs.current

    HStack {

        HStack(
            modifier = Modifier
                .clickable {
                    hintUi.create(
                        dialogsManager = navigation,
                        onSuccess = { checklistDb ->
                            navigation.push {
                                ChecklistFormItemsFs(
                                    checklistDb = checklistDb,
                                    onDelete = {},
                                )
                            }
                        },
                    )
                }
        ) {

            ZStack(
                modifier = Modifier
                    .padding(
                        start = HomeScreen__hPadding,
                        end = HomeScreen__itemCircleMarginTrailing,
                    )
                    .size(HomeScreen__itemCircleHeight)
                    .clip(roundedShape)
                    .background(c.white),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(id = R.drawable.sf_plus_medium_bold),
                    contentDescription = "Timer Info",
                    tint = c.black,
                    modifier = Modifier
                        .size(10.dp),
                )
            }

            Text(
                text = hintUi.title,
                modifier = Modifier
                    .height(HomeScreen__itemHeight),
                color = c.white,
                fontSize = HomeScreen__primaryFontSize,
            )
        }

        SpacerW1()

        ZStack(
            modifier = Modifier
                .padding(end = HomeScreen__hPadding)
                .size(HomeScreen__itemCircleHeight)
                .clip(roundedShape)
                .background(c.gray2)
                .clickable {
                    hintUi.hide()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(id = R.drawable.sf_xmark_small_medium),
                contentDescription = "Timer Info",
                tint = c.black,
                modifier = Modifier
                    .size(10.dp),
            )
        }
    }
}
