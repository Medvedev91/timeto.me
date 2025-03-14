package me.timeto.app.ui

import android.app.DownloadManager
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import kotlinx.coroutines.launch
import me.timeto.app.ui.checklists.ChecklistFormFs
import me.timeto.app.ui.checklists.ChecklistItemsFormFs
import me.timeto.app.ui.checklists.ChecklistScreen
import me.timeto.app.ui.form.FormButton
import me.timeto.app.ui.form.FormHeader
import me.timeto.app.ui.form.FormPaddingFirstItem
import me.timeto.app.ui.form.FormPaddingHeaderSection
import me.timeto.app.ui.form.FormPaddingSectionHeader
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationScreen
import me.timeto.app.ui.notes.NoteFormFs
import me.timeto.app.ui.notes.NoteFs
import me.timeto.app.ui.settings.SettingsDayStartFs
import me.timeto.app.ui.shortcuts.ShortcutFormFs
import me.timeto.app.ui.tasks.folders.TaskFoldersFormFs
import me.timeto.shared.*
import me.timeto.shared.ui.settings.SettingsVm
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsSheet(
    onClose: () -> Unit,
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val navigationFs = LocalNavigationFs.current
    val navigationScreen = LocalNavigationScreen.current

    BackHandler {
        onClose()
    }

    val (vm, state) = rememberVm {
        SettingsVm()
    }

    val launcherBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(),
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

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.headerTitle,
            scrollState = scrollState,
            actionButton = null,
            cancelButton = null,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
            contentPadding = PaddingValues(bottom = 25.dp),
        ) {

            item {

                FormPaddingFirstItem()

                FormButton(
                    title = state.readmeTitle,
                    isFirst = true,
                    isLast = false,
                    onClick = {
                        ReadmeSheet__show()
                    },
                )

                FormButton(
                    title = state.whatsNewTitle,
                    isFirst = false,
                    isLast = true,
                    note = state.whatsNewNote,
                    withArrow = true,
                    onClick = {
                        navigationScreen.push {
                            WhatsNewFs()
                        }
                    },
                )
            }

            //
            // Checklists

            val checklistsDb = state.checklistsDb

            item {
                FormPaddingSectionHeader()
                FormHeader(
                    title = "CHECKLISTS",
                )
                FormPaddingHeaderSection()
            }

            checklistsDb.forEach { checklistDb ->
                item {
                    FormButton(
                        title = checklistDb.name,
                        isFirst = checklistsDb.first() == checklistDb,
                        isLast = false,
                        withArrow = true,
                        onClick = {
                            navigationScreen.push {
                                ChecklistScreen(
                                    checklistDb = checklistDb,
                                )
                            }
                        },
                    )
                }
            }

            item {
                FormButton(
                    title = "New Checklist",
                    titleColor = c.blue,
                    isFirst = checklistsDb.isEmpty(),
                    isLast = true,
                    onClick = {
                        navigationFs.push {
                            ChecklistFormFs(
                                checklistDb = null,
                                onSave = { newChecklistDb ->
                                    navigationFs.push {
                                        ChecklistItemsFormFs(
                                            checklistDb = newChecklistDb,
                                            onDelete = {},
                                        )
                                    }
                                },
                                onDelete = {},
                            )
                        }
                    },
                )
            }

            //
            // Shortcuts

            val shortcutsDb = state.shortcutsDb

            item {
                FormPaddingSectionHeader()
                FormHeader(
                    title = "SHORTCUTS",
                )
                FormPaddingHeaderSection()
            }

            shortcutsDb.forEach { shortcutDb ->
                item {
                    FormButton(
                        title = shortcutDb.name,
                        isFirst = shortcutsDb.first() == shortcutDb,
                        isLast = false,
                        withArrow = false,
                        onClick = {
                            shortcutDb.performUi()
                        },
                        onLongClick = {
                            navigationFs.push {
                                ShortcutFormFs(
                                    shortcutDb = shortcutDb,
                                )
                            }
                        },
                    )
                }
            }

            item {
                FormButton(
                    title = "New Shortcut",
                    titleColor = c.blue,
                    isFirst = shortcutsDb.isEmpty(),
                    isLast = true,
                    onClick = {
                        navigationFs.push {
                            ShortcutFormFs(
                                shortcutDb = null,
                            )
                        }
                    },
                )
            }

            //
            // Notes

            val notesDb = state.notesDb

            item {
                FormPaddingSectionHeader()
                FormHeader(
                    title = "NOTES",
                )
                FormPaddingHeaderSection()
            }

            notesDb.forEach { noteDb ->
                item {
                    FormButton(
                        title = noteDb.title,
                        isFirst = notesDb.first() == noteDb,
                        isLast = false,
                        withArrow = true,
                        onClick = {
                            navigationFs.push {
                                NoteFs(
                                    initNoteDb = noteDb,
                                )
                            }
                        },
                    )
                }
            }

            item {
                FormButton(
                    title = "New Note",
                    titleColor = c.blue,
                    isFirst = notesDb.isEmpty(),
                    isLast = true,
                    onClick = {
                        navigationFs.push {
                            NoteFormFs(
                                noteDb = null,
                                onDelete = {},
                            )
                        }
                    },
                )
            }

            //
            // Settings

            item {

                FormPaddingSectionHeader()

                FormHeader("SETTINGS")

                FormPaddingHeaderSection()

                FormButton(
                    title = "Folders",
                    isFirst = true,
                    isLast = false,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            TaskFoldersFormFs()
                        }
                    },
                )

                FormButton(
                    title = "Day Start",
                    isFirst = false,
                    isLast = false,
                    note = state.dayStartNote,
                    onClick = {
                        navigationFs.push {
                            SettingsDayStartFs(
                                vm = vm,
                                state = state,
                            )
                        }
                    },
                )
            }

            item {

                MyListView__Padding__SectionHeader()

                MyListView__HeaderView(
                    "SETTINGS",
                )

                MyListView__Padding__HeaderSection()

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
