package me.timeto.app.ui.checklists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import me.timeto.app.R
import me.timeto.app.Haptic
import me.timeto.app.ui.HStack
import me.timeto.app.ui.TriggersIconsView
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.checklists.form.ChecklistFormItemFs
import me.timeto.app.ui.checklists.form.ChecklistFormItemsFs
import me.timeto.app.ui.home.HomeScreen__hPadding
import me.timeto.app.ui.home.HomeScreen__itemCircleHeight
import me.timeto.app.ui.home.HomeScreen__itemCircleMarginTrailing
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.HomeScreen__primaryFontSize
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.picker.NavigationPickerItem
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.vm.checklists.ChecklistVm
import me.timeto.shared.vm.checklists.ChecklistStateUi

private val checklistItemMinHeight = HomeScreen__itemHeight
private val itemFontSize: TextUnit = HomeScreen__primaryFontSize

private val checklistInnerIconPadding: Dp =
    (checklistItemMinHeight - HomeScreen__itemCircleHeight) / 2
private val checklistOuterHPadding: Dp =
    HomeScreen__hPadding - checklistInnerIconPadding

@Composable
fun ChecklistView(
    checklistDb: ChecklistDb,
    modifier: Modifier,
    scrollState: LazyListState,
    maxLines: Int,
    withAddButton: Boolean,
    topPadding: Dp,
    bottomPadding: Dp,
    withNavigationPadding: Boolean,
) {

    val (vm, state) = rememberVm(checklistDb) {
        ChecklistVm(checklistDb)
    }

    val navigationFs = LocalNavigationFs.current

    VStack(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val completionState = state.stateUi

        HStack(
            modifier = Modifier
                .padding(horizontal = checklistOuterHPadding)
                .weight(1f),
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = scrollState,
                contentPadding = PaddingValues(top = topPadding, bottom = bottomPadding),
            ) {

                state.itemsUi.forEach { itemUi ->

                    item {

                        HStack(
                            modifier = Modifier
                                .defaultMinSize(minHeight = checklistItemMinHeight)
                                .fillMaxWidth()
                                .clip(roundedShape)
                                .combinedClickable(
                                    onClick = {
                                        itemUi.toggle()
                                        Haptic.shot()
                                    },
                                    onLongClick = {
                                        navigationFs.picker(
                                            title = null,
                                            items = ChecklistItemContextItemType.buildItems(),
                                            onDone = { pickerItem ->
                                                when (pickerItem.item) {
                                                    ChecklistItemContextItemType.Edit -> {
                                                        navigationFs.push {
                                                            ChecklistFormItemFs(
                                                                checklistDb = checklistDb,
                                                                checklistItemDb = itemUi.itemDb,
                                                            )
                                                        }
                                                    }

                                                    ChecklistItemContextItemType.Delete -> {
                                                        vm.deleteItem(
                                                            itemDb = itemUi.itemDb,
                                                            dialogsManager = navigationFs,
                                                        )
                                                    }

                                                    ChecklistItemContextItemType.NewItem -> {
                                                        navigationFs.push {
                                                            ChecklistFormItemFs(
                                                                checklistDb = checklistDb,
                                                                checklistItemDb = null,
                                                            )
                                                        }
                                                    }

                                                    ChecklistItemContextItemType.EditChecklist -> {
                                                        navigationFs.push {
                                                            ChecklistFormItemsFs(
                                                                checklistDb = checklistDb,
                                                                onDelete = {},
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                        )
                                    },
                                )
                                .padding(start = checklistInnerIconPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {

                            ChecklistIconView(
                                iconType =
                                    if (itemUi.itemDb.isChecked)
                                        ChecklistIconType.checked
                                    else
                                        ChecklistIconType.unchecked,
                                contentDescription = "Checkbox",
                            )

                            Text(
                                text = itemUi.text,
                                color = c.white,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .padding(start = HomeScreen__itemCircleMarginTrailing),
                                fontSize = itemFontSize,
                                textAlign = TextAlign.Start,
                                maxLines = maxLines,
                                overflow = TextOverflow.Ellipsis,
                            )

                            TriggersIconsView(
                                checklistsDb = itemUi.textFeatures.checklistsDb,
                                shortcutsDb = itemUi.textFeatures.shortcutsDb,
                            )
                        }
                    }
                }

                if (withAddButton || state.itemsUi.isEmpty()) {
                    item {
                        ZStack(
                            modifier = Modifier
                                .height(checklistItemMinHeight),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = "New Item",
                                color = c.blue,
                                modifier = Modifier
                                    .clip(roundedShape)
                                    .clickable {
                                        navigationFs.push {
                                            ChecklistFormItemFs(
                                                checklistDb = checklistDb,
                                                checklistItemDb = null,
                                            )
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                                    .padding(horizontal = HomeScreen__hPadding),
                                fontSize = itemFontSize,
                            )
                        }
                    }
                }

                if (withNavigationPadding) {
                    item {
                        ZStack(Modifier.navigationBarsPadding())
                    }
                }
            }

            VStack(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(
                        start = 4.dp,
                        top = topPadding,
                        bottom = if (state.itemsUi.size > 1) 8.dp else 0.dp,
                    )
                    .clip(roundedShape)
                    .clickable {
                        completionState.onClick()
                        Haptic.shot()
                    },
            ) {

                ZStack(
                    modifier = Modifier
                        .size(checklistItemMinHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    ChecklistIconView(
                        iconType = when (completionState) {
                            is ChecklistStateUi.Completed -> ChecklistIconType.checked
                            is ChecklistStateUi.Empty -> ChecklistIconType.unchecked
                            is ChecklistStateUi.Partial -> ChecklistIconType.partial
                        },
                        contentDescription = completionState.actionDesc,
                    )
                }
            }
        }
    }
}

private sealed class ChecklistItemContextItemType {

    object Edit : ChecklistItemContextItemType()
    object Delete : ChecklistItemContextItemType()
    object NewItem : ChecklistItemContextItemType()
    object EditChecklist : ChecklistItemContextItemType()

    companion object {

        fun buildItems(): List<NavigationPickerItem<ChecklistItemContextItemType>> = listOf(
            NavigationPickerItem(
                title = "Edit",
                isSelected = false,
                item = Edit,
            ),
            NavigationPickerItem(
                title = "Delete",
                isSelected = false,
                item = Delete,
            ),
            NavigationPickerItem(
                title = "New Item",
                isSelected = false,
                item = NewItem,
            ),
            NavigationPickerItem(
                title = "Edit Checklist",
                isSelected = false,
                item = EditChecklist,
            ),
        )
    }
}

private enum class ChecklistIconType {
    checked, unchecked, partial
}

@Composable
private fun ChecklistIconView(
    iconType: ChecklistIconType,
    contentDescription: String,
) {
    val isFilled = iconType != ChecklistIconType.unchecked
    ZStack(
        modifier = Modifier
            .size(HomeScreen__itemCircleHeight)
            .clip(roundedShape)
            .background(if (isFilled) c.white else c.transparent)
            .border(2.dp, if (isFilled) c.transparent else c.homeFg, roundedShape),
        contentAlignment = Alignment.Center,
    ) {
        if (iconType == ChecklistIconType.checked) {
            Icon(
                painterResource(id = R.drawable.sf_checkmark_medium_semibold),
                contentDescription = contentDescription,
                tint = c.black,
                modifier = Modifier
                    .size(10.dp),
            )
        } else if (iconType == ChecklistIconType.partial) {
            Icon(
                painterResource(id = R.drawable.sf_minus_medium_semibold),
                contentDescription = "Checkbox",
                tint = c.black,
                modifier = Modifier
                    .size(10.dp),
            )
        }
    }
}
