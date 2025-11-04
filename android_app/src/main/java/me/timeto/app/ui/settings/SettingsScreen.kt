package me.timeto.app.ui.settings

import android.app.DownloadManager
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.timeto.app.LiveUpdatesUtils
import me.timeto.app.NotificationsUtils
import me.timeto.app.askAQuestion
import me.timeto.app.openNotificationSettings
import me.timeto.app.showOpenSource
import me.timeto.app.ui.HStack
import me.timeto.app.ui.LifecycleListener
import me.timeto.app.ui.Screen
import me.timeto.app.ui.c
import me.timeto.app.ui.whats_new.WhatsNewFs
import me.timeto.app.ui.checklists.form.ChecklistFormFs
import me.timeto.app.ui.checklists.form.ChecklistFormItemsFs
import me.timeto.app.ui.checklists.ChecklistScreen
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.FormHeader
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.form.padding.FormPaddingHeaderSection
import me.timeto.app.ui.form.padding.FormPaddingSectionHeader
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.form.FormSwitch
import me.timeto.app.ui.form.button.FormButtonEmoji
import me.timeto.app.ui.form.button.FormButtonView
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.home.settings.HomeSettingsButtonsFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationScreen
import me.timeto.app.ui.navigation.NavigationAlert
import me.timeto.app.ui.notes.NoteFormFs
import me.timeto.app.ui.notes.NoteFs
import me.timeto.app.ui.privacy.PrivacyFs
import me.timeto.app.ui.readme.Readme2Fs
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.shortcuts.ShortcutFormFs
import me.timeto.app.ui.tasks.folders.TaskFoldersFormFs
import me.timeto.app.ui.timer.TimerSheet
import me.timeto.shared.*
import me.timeto.shared.backups.Backup
import me.timeto.shared.vm.settings.SettingsVm
import me.timeto.shared.performUi
import java.io.BufferedReader
import java.io.InputStreamReader

private const val persistentNotificationText = "Persistent Notification"

@Composable
fun SettingsScreen(
    onClose: () -> Unit,
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val navigationFs = LocalNavigationFs.current
    val navigationScreen = LocalNavigationScreen.current

    BackHandler {
        onClose()
    }

    val isLiveUpdatesSystemEnabled = remember {
        mutableStateOf(isLiveUpdatesSystemEnabled())
    }

    LifecycleListener { _, event ->
        if (event == Lifecycle.Event.ON_RESUME)
            isLiveUpdatesSystemEnabled.value = isLiveUpdatesSystemEnabled()
    }

    val (vm, state) = rememberVm {
        SettingsVm()
    }

    val launcherBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(),
    ) { destinationUri ->

        if (destinationUri == null) {
            navigationFs.alert("File not selected")
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            try {
                val jsonBytes = Backup.create("manual").toByteArray()
                val stream = context.contentResolver.openOutputStream(destinationUri) ?: throw Exception()
                stream.write(jsonBytes)
                stream.close()
            } catch (e: Exception) {
                navigationFs.alert("Error")
                reportApi("launcherBackup exception:\n$e")
            }
        }
    }

    val launcherRestore = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { destinationUri ->

        if (destinationUri == null) {
            navigationFs.alert("File not selected")
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
                navigationFs.alert("Error")
                reportApi("launcherRestore exception:\n$e")
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

                FormPaddingTop()

                FormButton(
                    title = state.readmeTitle,
                    isFirst = true,
                    isLast = false,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            // ReadmeFs()
                            Readme2Fs()
                        }
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
            // Goals

            item {
                FormPaddingSectionHeader()
                FormHeader(
                    title = "GOALS",
                )
                FormPaddingHeaderSection()
            }

            item {

                val goalsUi = state.goalsUi
                goalsUi.forEach { goalUi ->
                    FormButtonView(
                        title = goalUi.title,
                        titleColor = null,
                        isFirst = goalsUi.first() == goalUi,
                        isLast = false,
                        modifier = Modifier,
                        rightView = {

                            HStack(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(end = 8.dp),
                            ) {

                                goalUi.timerHintsUi.forEach { timerHintUi ->
                                    Text(
                                        text = timerHintUi.title,
                                        modifier = Modifier
                                            .clip(roundedShape)
                                            .clickable {
                                                timerHintUi.onTap()
                                            }
                                            .padding(horizontal = 8.dp),
                                        color = c.blue,
                                    )
                                }
                            }
                        },
                        onClick = {
                            navigationFs.push {
                                TimerSheet(
                                    title = goalUi.title,
                                    doneTitle = "Start",
                                    initSeconds = 45 * 60,
                                    onDone = { seconds ->
                                        vm.startInterval(
                                            goalDb = goalUi.goalDb,
                                            seconds = seconds,
                                        )
                                    },
                                )
                            }
                        },
                        onLongClick = null,
                    )
                }

                FormButton(
                    title = state.goalsTitle,
                    titleColor = c.blue,
                    isFirst = false,
                    isLast = true,
                    onClick = {
                        navigationFs.push {
                            HomeSettingsButtonsFs()
                        }
                        scope.launch {
                            delay(1_000)
                            onClose()
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
                                    withNavigationPadding = false,
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
                                        ChecklistFormItemsFs(
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
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            SettingsDayStartFs(
                                vm = vm,
                                state = state,
                            )
                        }
                    },
                )

                FormSwitch(
                    title = state.todayOnHomeScreenText,
                    isEnabled = state.todayOnHomeScreen,
                    isFirst = false,
                    isLast = true,
                    onChange = { newValue ->
                        vm.setTodayOnHomeScreen(isOn = newValue)
                    },
                )
            }

            //
            // Backups

            item {

                FormPaddingSectionHeader()

                FormHeader(
                    title = "BACKUPS",
                )

                FormPaddingHeaderSection()

                FormButton(
                    title = "Create",
                    isFirst = true,
                    isLast = false,
                    onClick = {
                        scope.launch {
                            launcherBackup.launch(vm.prepBackupFileName())
                        }
                    },
                )

                FormButton(
                    title = "Restore",
                    isFirst = false,
                    isLast = false,
                    onClick = {
                        scope.launch {
                            launcherRestore.launch("*/*")
                        }
                    },
                )

                FormButton(
                    title = "Auto Backup",
                    isFirst = false,
                    isLast = true,
                    note = state.autoBackupTimeString,
                    withArrow = true,
                    onClick = {
                        context.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                    },
                )
            }

            //
            // Notifications

            item {

                FormPaddingSectionHeader()

                FormHeader(
                    title = "NOTIFICATIONS",
                )

                FormPaddingHeaderSection()

                FormButton(
                    title = "Time to Break",
                    isFirst = true,
                    isLast = false,
                    onClick = {
                        openNotificationChannelSettings(
                            context = context,
                            channel = NotificationsUtils.channelTimerExpired(),
                        )
                    },
                )

                FormButton(
                    title = "Timer Overdue",
                    isFirst = false,
                    isLast = isLiveUpdatesSystemEnabled.value,
                    onClick = {
                        openNotificationChannelSettings(
                            context = context,
                            channel = NotificationsUtils.channelTimerOverdue(),
                        )
                    },
                )

                if (!isLiveUpdatesSystemEnabled.value) {
                    FormButton(
                        title = persistentNotificationText,
                        titleColor = c.red,
                        isFirst = false,
                        isLast = true,
                        note = "Not Granted",
                        noteColor = c.red,
                        withArrow = true,
                        arrowColor = c.red,
                        onClick = {
                            navigationFs.dialog { dialogLayer ->
                                NavigationAlert(
                                    message = "Please enable \"Live updates\" in settings.",
                                    withCancelButton = true,
                                    buttonText = "Settings",
                                    buttonColor = c.blue,
                                    onButtonClick = {
                                        openNotificationSettings(context)
                                        dialogLayer.close()
                                    },
                                )
                            }
                        },
                    )
                }
            }

            //
            // Misc

            item {

                FormPaddingSectionSection()

                FormButton(
                    title = "Ask a Question",
                    isFirst = true,
                    isLast = false,
                    onClick = {
                        askAQuestion(subject = state.feedbackSubject)
                    },
                )

                FormButton(
                    title = "Open Source",
                    isFirst = false,
                    isLast = false,
                    onClick = {
                        showOpenSource()
                    },
                )

                FormButtonEmoji(
                    title = "Privacy",
                    emoji = state.privacyEmoji ?: "",
                    isFirst = false,
                    isLast = true,
                    onClick = {
                        navigationFs.push {
                            PrivacyFs(isFdroid = false)
                        }
                    },
                )
            }

            item {
                Row {
                    Text(
                        text = state.infoText,
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .weight(1f),
                        color = c.secondaryText,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }
}

///

private fun openNotificationChannelSettings(
    context: Context,
    channel: NotificationChannel,
) {
    context.startActivity(
        Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, channel.id)
        }
    )
}

private fun isLiveUpdatesSystemEnabled(): Boolean {
    if (LiveUpdatesUtils.isSdkAvailable())
        return NotificationsUtils.manager.canPostPromotedNotifications()
    return true
}
