package me.timeto.app.ui

import android.app.DownloadManager
import android.content.Intent
import android.provider.Settings
import android.widget.NumberPicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.vm.PrivacySheetVm
import me.timeto.shared.vm.SettingsVm
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsSheet(
    layer: WrapperView.Layer,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val (vm, state) = rememberVm { SettingsVm() }

    val launcherBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { destinationUri ->

        if (destinationUri == null) {
            showUiAlert("File not selected")
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            try {
                val jsonBytes = Backup.create("manual").toByteArray()
                val stream = context.contentResolver.openOutputStream(destinationUri) ?: throw Exception()
                stream.write(jsonBytes)
                stream.close()
                // todo
                zlog("ok")
            } catch (e: Exception) {
                showUiAlert("Error", "launcherBackup exception:\n$e")
            }
        }
    }

    val launcherRestore = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { destinationUri ->

        if (destinationUri == null) {
            showUiAlert("File not selected")
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(destinationUri)
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var receiveString: String? = ""
                val stringBuilder = StringBuilder()
                while (bufferedReader.readLine().also { receiveString = it } != null) {
                    stringBuilder.append("\n").append(receiveString)
                }
                inputStream!!.close()
                val jString = stringBuilder.toString()
                vm.procRestore(jString)
            } catch (e: Exception) {
                showUiAlert("Error", "launcherRestore exception:\n$e")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.bg),
    ) {

        val scrollState = rememberLazyListState()

        Fs__HeaderTitle(
            title = state.headerTitle,
            scrollState = scrollState,
            onClose = {
                layer.close()
            },
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
            contentPadding = PaddingValues(bottom = 25.dp),
        ) {

            item {

                MyListView__PaddingFirst()

                MyListView__ItemView(
                    isFirst = true,
                    isLast = false,
                    withTopDivider = false,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = state.readmeTitle,
                        bgColor = c.fg,
                    ) {
                        ReadmeSheet__show()
                    }
                }

                MyListView__ItemView(
                    isFirst = false,
                    isLast = true,
                    withTopDivider = true,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = state.whatsNewTitle,
                        bgColor = c.fg,
                        rightView = {
                            MyListView__ItemView__ButtonView__RightText(
                                text = state.whatsNewNote,
                            )
                        },
                    ) {
                        Fs.show { layer ->
                            WhatsNewFs(layer)
                        }
                    }
                }
            }

            item {

                MyListView__Padding__SectionHeader()

                MyListView__HeaderView(
                    title = "CHECKLISTS",
                    rightView = {
                        MyListView__HeaderView__RightIcon(
                            icon = Icons.Rounded.Add,
                            contentDescription = "New Checklist"
                        ) {
                            Dialog.show { layer ->
                                ChecklistNameDialog(
                                    layer = layer,
                                    editedChecklist = null,
                                    onSave = {},
                                )
                            }
                        }
                    }
                )
            }

            val checklists = state.checklists
            if (checklists.isNotEmpty())
                item { MyListView__Padding__HeaderSection() }

            itemsIndexed(checklists, key = { _, checklist -> checklist.id }) { _, checklist ->

                val isFirst = checklists.first() == checklist

                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = checklists.last() == checklist,
                    withTopDivider = !isFirst,
                ) {

                    SwipeToAction(
                        isStartOrEnd = remember { mutableStateOf(null) },
                        startView = {
                            SwipeToAction__StartView(
                                text = "Edit",
                                bgColor = c.blue
                            )
                        },
                        endView = { state ->
                            SwipeToAction__DeleteView(
                                state = state,
                                note = checklist.name,
                                deletionConfirmationNote = "Are you sure you want to delete \"${checklist.name}\" checklist?",
                            ) {
                                vibrateLong()
                                scope.launchEx {
                                    checklist.deleteWithDependencies()
                                }
                            }
                        },
                        onStart = {
                            Dialog.show { layer ->
                                ChecklistNameDialog(
                                    layer = layer,
                                    editedChecklist = checklist,
                                    onSave = {},
                                )
                            }
                            false
                        },
                        onEnd = {
                            true
                        },
                        toVibrateStartEnd = listOf(true, false),
                    ) {

                        MyListView__ItemView__ButtonView(
                            text = checklist.name,
                            bgColor = c.fg,
                        ) {
                            checklist.performUI()
                        }
                    }
                }
            }

            item {

                MyListView__Padding__SectionHeader((-9).dp) // ~9.dp consume icon space

                MyListView__HeaderView(
                    title = "SHORTCUTS",
                    rightView = {
                        MyListView__HeaderView__RightIcon(
                            icon = Icons.Rounded.Add,
                            contentDescription = "New Shortcut"
                        ) {
                            Sheet.show { layer ->
                                ShortcutFormSheet(layer = layer, editedShortcut = null)
                            }
                        }
                    }
                )
            }

            val shortcuts = state.shortcuts
            if (shortcuts.isNotEmpty())
                item { MyListView__Padding__HeaderSection() }

            itemsIndexed(shortcuts, key = { _, shortcut -> shortcut.id }) { _, shortcut ->

                val isFirst = shortcuts.first() == shortcut

                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = shortcuts.last() == shortcut,
                    withTopDivider = !isFirst,
                ) {

                    SwipeToAction(
                        isStartOrEnd = remember { mutableStateOf(null) },
                        startView = {
                            SwipeToAction__StartView(
                                text = "Edit",
                                bgColor = c.blue
                            )
                        },
                        endView = { state ->
                            SwipeToAction__DeleteView(
                                state = state,
                                note = shortcut.name,
                                deletionConfirmationNote = "Are you sure you want to delete \"${shortcut.name}\" shortcut?",
                            ) {
                                vibrateLong()
                                scope.launchEx {
                                    shortcut.delete()
                                }
                            }
                        },
                        onStart = {
                            Sheet.show { layer ->
                                ShortcutFormSheet(layer = layer, editedShortcut = shortcut)
                            }
                            false
                        },
                        onEnd = {
                            true
                        },
                        toVibrateStartEnd = listOf(true, false),
                    ) {
                        MyListView__ItemView__ButtonView(
                            text = shortcut.name,
                            bgColor = c.fg,
                        ) {
                            shortcut.performUI()
                        }
                    }
                }
            }

            item {

                MyListView__Padding__SectionHeader((-9).dp) // ~9.dp consume icon space

                MyListView__HeaderView(
                    title = "NOTES",
                    rightView = {
                        MyListView__HeaderView__RightIcon(
                            icon = Icons.Rounded.Add,
                            contentDescription = "New Note"
                        ) {
                            Sheet.show { layer ->
                                NoteFormSheet(layer, note = null, onDelete = {})
                            }
                        }
                    }
                )
            }

            val notes = state.notes
            if (notes.isNotEmpty())
                item { MyListView__Padding__HeaderSection() }

            itemsIndexed(
                items = notes,
                key = { _, note -> note.id },
            ) { _, note ->

                val isFirst = notes.first() == note

                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = notes.last() == note,
                    withTopDivider = !isFirst,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = note.title,
                        maxLines = 1,
                        bgColor = c.fg,
                    ) {
                        Sheet.show { layer ->
                            NoteSheet(layer, initNote = note)
                        }
                    }
                }
            }

            item {

                MyListView__Padding__SectionHeader()

                MyListView__HeaderView(
                    "SETTINGS",
                )

                MyListView__Padding__HeaderSection()

                MyListView__ItemView(
                    isFirst = true,
                    isLast = false,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = "Folders",
                        withArrow = true,
                        bgColor = c.fg,
                    ) {
                        Sheet.show { layer ->
                            FoldersSettingsSheet(layer)
                        }
                    }
                }

                //////

                MyListView__ItemView(
                    isFirst = false,
                    isLast = false,
                    withTopDivider = true,
                ) {

                    MyListView__ItemView__ButtonView(
                        text = "Day Start",
                        withArrow = false,
                        bgColor = c.fg,
                        rightView = {
                            MyListView__ItemView__ButtonView__RightText(
                                text = state.dayStartNote
                            )
                        }
                    ) {
                        Dialog.show { layer ->
                            DayStartDialogView(
                                settingsSheetVM = vm,
                                settingsSheetState = state,
                                onClose = layer::close
                            )
                        }
                    }
                }

                MyListView__ItemView(
                    isFirst = false,
                    isLast = true,
                    withTopDivider = true,
                    bgColor = c.fg,
                ) {

                    MyListView__ItemView__SwitchView(
                        text = state.todayOnHomeScreenText,
                        isActive = state.todayOnHomeScreen,
                    ) {
                        vm.toggleTodayOnHomeScreen()
                    }
                }
            }

            item {

                MyListView__Padding__SectionHeader()

                MyListView__HeaderView(
                    "BACKUPS",
                )

                MyListView__Padding__HeaderSection()

                MyListView__ItemView(
                    isFirst = true,
                    isLast = false,
                    withTopDivider = false,
                ) {

                    MyListView__ItemView__ButtonView(
                        text = "Create",
                        bgColor = c.fg,
                    ) {
                        scope.launch {
                            launcherBackup.launch(vm.prepBackupFileName())
                        }
                    }
                }

                MyListView__ItemView(
                    isFirst = false,
                    isLast = false,
                    withTopDivider = true,
                ) {

                    MyListView__ItemView__ButtonView(
                        text = "Restore",
                        bgColor = c.fg,
                    ) {
                        scope.launch {
                            launcherRestore.launch("*/*")
                        }
                    }
                }

                MyListView__ItemView(
                    isFirst = false,
                    isLast = true,
                    withTopDivider = true,
                ) {

                    if (isSDKQPlus()) {
                        MyListView__ItemView__ButtonView(
                            text = "Auto Backup",
                            withArrow = true,
                            bgColor = c.fg,
                            rightView = {
                                MyListView__ItemView__ButtonView__RightText(
                                    text = state.autoBackupTimeString,
                                    paddingEnd = 4.dp,
                                )
                            }
                        ) {
                            // todo doc about folder
                            context.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                        }
                    }
                }
            }

            item {

                MyListView__Padding__SectionHeader()

                MyListView__HeaderView(
                    title = "NOTIFICATIONS",
                )

                MyListView__Padding__HeaderSection()

                MyListView__ItemView(
                    isFirst = true,
                    isLast = false,
                    withTopDivider = false,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = "Time to Break",
                        bgColor = c.fg,
                    ) {
                        context.startActivity(
                            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                putExtra(Settings.EXTRA_CHANNEL_ID, NotificationCenter.channelTimerExpired().id)
                            }
                        )
                    }
                }

                MyListView__ItemView(
                    isFirst = false,
                    isLast = true,
                    withTopDivider = true,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = "Timer Overdue",
                        bgColor = c.fg,
                    ) {
                        context.startActivity(
                            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                putExtra(Settings.EXTRA_CHANNEL_ID, NotificationCenter.channelTimerOverdue().id)
                            }
                        )
                    }
                }
            }

            item {

                MyListView__Padding__SectionSection()

                MyListView__ItemView(
                    isFirst = true,
                    isLast = false,
                    withTopDivider = false,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = "Ask a Question",
                        bgColor = c.fg,
                    ) {
                        askAQuestion(subject = state.feedbackSubject)
                    }
                }

                MyListView__ItemView(
                    isFirst = false,
                    isLast = false,
                    withTopDivider = true,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = "Open Source",
                        bgColor = c.fg,
                    ) {
                        showOpenSource()
                    }
                }

                MyListView__ItemView(
                    isFirst = false,
                    isLast = true,
                    withTopDivider = true,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = "Privacy",
                        bgColor = c.fg,
                        rightView = {
                            val privacyNote = state.privacyNote
                            if (privacyNote == null)
                                Text("")
                            else
                                Text(
                                    text = PrivacySheetVm.prayEmoji,
                                    modifier = Modifier
                                        .padding(end = H_PADDING),
                                    fontSize = 18.sp,
                                )
                        },
                    ) {
                        Sheet.show { layer ->
                            PrivacySheet(layer)
                        }
                    }
                }
            }

            item {

                Row {

                    Text(
                        text = "timeto.me for Android\nv${BuildConfig.VERSION_NAME}.${state.appVersion}-${BuildConfig.FLAVOR}",
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .weight(1f),
                        color = c.textSecondary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                )
            }
        }
    }
}

@Composable
private fun DayStartDialogView(
    settingsSheetVM: SettingsVm,
    settingsSheetState: SettingsVm.State,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(c.sheetBg)
            .padding(20.dp)
    ) {

        Text(
            "Day Start",
            fontSize = 24.sp,
            fontWeight = FontWeight.W500,
            color = c.text,
        )

        val items = settingsSheetState.dayStartListItems
        var selectedItem by remember { mutableIntStateOf(settingsSheetState.dayStartSelectedIdx) }

        AndroidView(
            modifier = Modifier
                .padding(top = 10.dp)
                .size(100.dp, 150.dp)
                .align(Alignment.CenterHorizontally),
            factory = { context ->
                NumberPicker(context).apply {
                    displayedValues = items.map { it.note }.toTypedArray()
                    setOnValueChangedListener { _, _, new ->
                        selectedItem = new
                    }
                    wrapSelectorWheel = false
                    minValue = 0
                    maxValue = items.size - 1
                    value = selectedItem // Set last
                }
            }
        )

        //
        //

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                "Cancel",
                color = c.textSecondary,
                modifier = Modifier
                    .padding(end = 11.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClose() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            MyButton("Save", true, c.blue) {
                settingsSheetVM.upDayStartOffsetSeconds(items[selectedItem].seconds) {
                    onClose()
                }
            }
        }
    }
}
