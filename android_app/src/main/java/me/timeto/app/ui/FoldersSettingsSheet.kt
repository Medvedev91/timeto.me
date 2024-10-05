package me.timeto.app.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.app.roundedShape
import me.timeto.shared.vm.FoldersSettingsVm

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoldersSettingsSheet(
    layer: WrapperView.Layer,
) {
    val (vm, state) = rememberVm { FoldersSettingsVm() }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg)
    ) {

        val scrollState = rememberLazyListState()

        Sheet.HeaderViewOld(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = null,
            isDoneEnabled = false,
            scrollState = scrollState,
            cancelText = "Back",
        ) {}

        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp)
        ) {

            val folders = state.folders
            items(
                items = folders,
                key = { folder -> folder.id }
            ) { folder ->

                val isFirst = folders.first() == folder

                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = folders.last() == folder,
                    withTopDivider = !isFirst,
                    modifier = Modifier.animateItemPlacement(),
                ) {
                    MyListView__ItemView__ButtonView(
                        text = folder.name,
                        rightView = {
                            Icon(
                                Icons.Rounded.ArrowUpward,
                                "Up",
                                tint = c.blue,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(26.dp)
                                    .clip(roundedShape)
                                    .clickable(!isFirst) {
                                        vm.sortUp(folder)
                                    }
                                    .padding(2.dp)
                            )
                        }
                    ) {
                        Sheet.show { layer ->
                            FolderFormSheet(
                                layer = layer,
                                folder = folder,
                            )
                        }
                    }
                }
            }

            item {

                MyListView__Padding__SectionSection()

                MyListView__ItemView(
                    isFirst = true,
                    isLast = true,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = "New Folder",
                    ) {
                        Sheet.show { layer ->
                            FolderFormSheet(
                                layer = layer,
                                folder = null,
                            )
                        }
                    }
                }
            }

            ////

            val tmrwButtonUi = state.tmrwButtonUi
            if (tmrwButtonUi != null) {
                item {

                    MyListView__Padding__SectionSection()

                    MyListView__ItemView(
                        isFirst = true,
                        isLast = true,
                    ) {
                        MyListView__ItemView__ButtonView(
                            text = tmrwButtonUi.text,
                        ) {
                            tmrwButtonUi.add()
                        }
                    }
                }
            }
        }
    }
}
