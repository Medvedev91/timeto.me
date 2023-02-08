package app.time_to.timeto.ui

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.NumberPicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
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
import app.time_to.timeto.*
import app.time_to.timeto.R
import kotlinx.coroutines.launch
import timeto.shared.Backup
import timeto.shared.launchEx
import timeto.shared.reportApi
import timeto.shared.vm.TabToolsVM
import timeto.shared.zlog
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TabToolsView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val (vm, state) = rememberVM { TabToolsVM() }

    val errorDialog = LocalErrorDialog.current

    val launcherBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { destinationUri ->
        // todo check if null destinationUri. On cancel.
        scope.launch {
            try {
                val jsonBytes = Backup.create("manual").toByteArray()
                val stream = context.contentResolver.openOutputStream(destinationUri!!) ?: throw Exception()
                stream.write(jsonBytes)
                stream.close()
                // todo
                zlog("ok")
            } catch (e: Exception) {
                errorDialog.value = "Error"
                reportApi("launcherBackup exception:\n$e")
            }
        }
    }

    val launcherRestore = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { destinationUri ->
        // todo check if null destinationUri. On cancel.
        // todo handle errors
        scope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(destinationUri!!)
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var receiveString: String? = ""
                val stringBuilder = StringBuilder()
                while (bufferedReader.readLine().also { receiveString = it } != null) {
                    stringBuilder.append("\n").append(receiveString)
                }
                inputStream!!.close()

                val jString = stringBuilder.toString()

                Backup.restore(jString)

                context.startActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            } catch (e: Exception) {
                errorDialog.value = "Error"
                reportApi("launcherRestore exception:\n$e")
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background),
        contentPadding = PaddingValues(top = MyListView.PADDING_SECTION_HEADER, bottom = 25.dp)
    ) {

        item {

            MyListView__HeaderView(
                title = "CHECKLISTS",
                rightView = {
                    val isChecklistNewPresented = remember { mutableStateOf(false) }
                    ChecklistEditDialog(editedChecklist = null, isPresented = isChecklistNewPresented)

                    MyListView__HeaderView__RightIcon(
                        iconId = R.drawable.ic_round_add_24,
                        contentDescription = "New Checklist"
                    ) {
                        isChecklistNewPresented.value = true
                    }
                }
            )
        }

        val checklists = state.checklists
        itemsIndexed(checklists, key = { _, checklist -> checklist.id }) { _, checklist ->

            val isChecklistEditPresented = remember { mutableStateOf(false) }
            ChecklistEditDialog(editedChecklist = checklist, isPresented = isChecklistEditPresented)

            val isChecklistPresented = remember { mutableStateOf(false) }
            ChecklistDialog(checklist = checklist, isPresented = isChecklistPresented)

            val isFirst = checklists.first() == checklist

            MyListView__ItemView(
                isFirst = isFirst,
                isLast = checklists.last() == checklist,
                withTopDivider = !isFirst,
                modifier = Modifier.padding(top = if (isFirst) MyListView.PADDING_HEADER_SECTION else 0.dp),
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
                        isChecklistEditPresented.value = true
                        false
                    },
                    onEnd = {
                        true
                    },
                    toVibrateStartEnd = listOf(true, false),
                ) {
                    MyListView__ItemView__ButtonView(
                        text = checklist.name,
                    ) {
                        isChecklistPresented.value = true
                    }
                }
            }
        }

        item {

            MyListView__HeaderView(
                title = "SHORTCUTS",
                modifier = Modifier.padding(top = MyListView.PADDING_SECTION_HEADER - 9.dp), // ~9.dp consume icon space
                rightView = {
                    val isAddShortcutPresented = remember { mutableStateOf(false) }
                    ShortcutFormSheet(isPresented = isAddShortcutPresented, editedShortcut = null)

                    MyListView__HeaderView__RightIcon(
                        iconId = R.drawable.ic_round_add_24,
                        contentDescription = "New Shortcut"
                    ) {
                        isAddShortcutPresented.value = true
                    }
                }
            )
        }

        val shortcuts = state.shortcuts
        itemsIndexed(shortcuts, key = { _, shortcut -> shortcut.id }) { _, shortcut ->

            val isShortcutEditPresented = remember { mutableStateOf(false) }
            ShortcutFormSheet(isPresented = isShortcutEditPresented, editedShortcut = shortcut)

            val isFirst = shortcuts.first() == shortcut

            MyListView__ItemView(
                isFirst = isFirst,
                isLast = shortcuts.last() == shortcut,
                withTopDivider = !isFirst,
                modifier = Modifier.padding(top = if (isFirst) MyListView.PADDING_HEADER_SECTION else 0.dp),
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
                        isShortcutEditPresented.value = true
                        false
                    },
                    onEnd = {
                        true
                    },
                    toVibrateStartEnd = listOf(true, false),
                ) {
                    MyListView__ItemView__ButtonView(
                        text = shortcut.name,
                    ) {
                        performShortcutOrError(shortcut, context, errorDialog)
                    }
                }
            }
        }

        item {

            MyListView__HeaderView(
                "SETTINGS",
                modifier = Modifier
                    .padding(top = MyListView.PADDING_SECTION_HEADER),
            )

            MyListView__ItemView(
                isFirst = true,
                isLast = false,
                modifier = Modifier.padding(top = MyListView.PADDING_HEADER_SECTION),
            ) {

                val isFoldersSettingsPresented = remember { mutableStateOf(false) }
                FoldersSettingsSheet(isFoldersSettingsPresented)
                MyListView__ItemView__ButtonView(
                    text = "Folders",
                    withArrow = true,
                ) {
                    isFoldersSettingsPresented.trueValue()
                }
            }

            //////

            val isDayStartPresented = remember { mutableStateOf(false) }
            DayStartDialog(
                isPresented = isDayStartPresented,
                tabToolsVM = vm,
                tabToolsState = state,
            )

            MyListView__ItemView(
                isFirst = false,
                isLast = true,
                withTopDivider = true,
            ) {

                MyListView__ItemView__ButtonView(
                    text = "Day Start",
                    withArrow = false,
                    rightView = {
                        MyListView__ItemView__ButtonView__RightText(
                            text = state.dayStartNote
                        )
                    }
                ) {
                    isDayStartPresented.value = true
                }
            }
        }

        item {

            MyListView__HeaderView(
                "BACKUPS",
                modifier = Modifier
                    .padding(top = MyListView.PADDING_SECTION_HEADER),
            )

            MyListView__ItemView(
                isFirst = true,
                isLast = false,
                modifier = Modifier.padding(top = MyListView.PADDING_HEADER_SECTION),
                withTopDivider = false,
            ) {

                MyListView__ItemView__ButtonView(
                    text = "Create",
                ) {
                    scope.launch {
                        val date = DateFormat.format("yyyyMMdd_HHmmss", Date())
                        launcherBackup.launch("timeto_${date}.json")
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

                val autoBackup = LocalAutoBackup.current
                if (autoBackup != null && isSDKQPlus()) {

                    val dateStr = autoBackup.lastCacheDate.value?.let { date ->
                        val calendar = Calendar.getInstance(Locale.ENGLISH)
                        val format = "d MMM, E HH:mm" // todo is24
                        calendar.timeInMillis = date.time
                        DateFormat.format(format, calendar).toString()
                    }

                    MyListView__ItemView__ButtonView(
                        text = "Auto Backup",
                        withArrow = true,
                        rightView = {
                            MyListView__ItemView__ButtonView__RightText(
                                text = dateStr ?: "",
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

            MyListView__HeaderView(
                title = "NOTIFICATIONS",
                modifier = Modifier.padding(top = MyListView.PADDING_SECTION_HEADER),
            )

            MyListView__ItemView(
                isFirst = true,
                isLast = false,
                modifier = Modifier.padding(top = MyListView.PADDING_HEADER_SECTION),
                withTopDivider = false,
            ) {
                MyListView__ItemView__ButtonView(text = "Time to Break") {
                    context.startActivity(
                        Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            putExtra(Settings.EXTRA_CHANNEL_ID, NotificationCenter.channelTimeToBreak().id)
                        }
                    )
                }
            }

            MyListView__ItemView(
                isFirst = false,
                isLast = true,
                withTopDivider = true,
            ) {
                MyListView__ItemView__ButtonView(text = "Timer Overdue") {
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

            MyListView__ItemView(
                isFirst = true,
                isLast = false,
                modifier = Modifier.padding(top = MyListView.PADDING_SECTION_SECTION),
                withTopDivider = false,
            ) {
                MyListView__ItemView__ButtonView(text = "Ask a Question") {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("mailto:${state.feedbackEmail}?subject=${state.feedbackSubject}")
                        }
                    )
                }
            }

            MyListView__ItemView(
                isFirst = false,
                isLast = true,
                withTopDivider = true,
            ) {
                MyListView__ItemView__ButtonView(text = "Open Source") {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(state.openSourceUrl)
                        }
                    )
                }
            }
        }

        item {

            val isReadmePresented = remember { mutableStateOf(false) }
            ReadmeView(isPresented = isReadmePresented)

            /*
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .clip(MySquircleShape(angles = listOf(true, true, false, false)))
                    .background(c.background2)
                    .clickable {
                        isReadmePresented.value = true
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Text(
                    "Readme",
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .align(Alignment.CenterStart),
                    color = c.text,
                )

                Divider(
                    color = c.dividerBackground2,
                    modifier = Modifier.padding(start = 18.dp),
                    thickness = 0.5.dp
                )
            }
             */
        }

        item {

            Row {

                Text(
                    text = "TimeTo for Android v${state.appVersion}",
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .weight(1f),
                    color = c.textSecondary,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun DayStartDialog(
    isPresented: MutableState<Boolean>,
    tabToolsVM: TabToolsVM,
    tabToolsState: TabToolsVM.State,
) {
    if (!isPresented.value)
        return

    MyDialog(isPresented) {

        Column {

            Text(
                "Day Start",
                fontSize = 24.sp,
                fontWeight = FontWeight.W500,
            )

            val items = tabToolsState.dayStartListItems
            var selectedItem by remember { mutableStateOf(tabToolsState.dayStartSelectedIdx) }

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
                        .clickable { isPresented.value = false }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                MyButton("Save", true, c.blue) {
                    tabToolsVM.upDayStartOffsetSeconds(items[selectedItem].seconds) {
                        isPresented.value = false
                    }
                }
            }
        }
    }
}
