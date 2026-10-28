package me.timeto.app.ui.doc

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import me.timeto.app.ui.Screen
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.rememberVm
import me.timeto.shared.launchExIo
import me.timeto.shared.vm.doc.DocVm

@Composable
fun DocFs(
    forceRead: Boolean,
) {

    val navigationLayer = LocalNavigationLayer.current
    val scrollState = rememberLazyListState()

    val (vm, state) = rememberVm {
        DocVm()
    }

    DisposableEffect(Unit) {
        launchExIo { docFsIsOpenFlow.emit(true) }
        onDispose {
            launchExIo { docFsIsOpenFlow.emit(false) }
        }
    }

    Screen {

        Header(
            title = "How to Use the App",
            scrollState = scrollState,
            actionButton = null,
            cancelButton = if (forceRead) null else {
                HeaderCancelButton(
                    text = "Close",
                    onClick = {
                        navigationLayer.close()
                    },
                )
            },
        )

        BackHandler(forceRead) {
        }

        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .weight(1f),
        ) {
            DocEnView(
                vm = vm,
                state = state,
                forceRead = forceRead,
            )
        }
    }
}
