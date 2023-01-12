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
        contentPadding = PaddingValues(bottom = 25.dp)
    ) {

        item {

            MyList.Header("CHECKLISTS") {

                val isChecklistNewPresented = remember { mutableStateOf(false) }
                ChecklistEditDialog(editedChecklist = null, isPresented = isChecklistNewPresented)

                MyList.Header__RightIcon(
                    iconId = R.drawable.ic_round_add_24,
                    contentDescription = "New Checklist"
                ) {
                    isChecklistNewPresented.value = true
                }
            }
        }

        val checklists = state.checklists
        itemsIndexed(checklists, key = { _, checklist -> checklist.id }) { _, checklist ->

            val isChecklistEditPresented = remember { mutableStateOf(false) }
            ChecklistEditDialog(editedChecklist = checklist, isPresented = isChecklistEditPresented)

            val isChecklistPresented = remember { mutableStateOf(false) }
            ChecklistDialog(checklist = checklist, isPresented = isChecklistPresented)

            MyList.SectionItem(
                isFirst = checklists.first() == checklist,
                isLast = checklists.last() == checklist,
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
                    MyList.SectionItem_Button(
                        text = checklist.name,
                        withDivider = checklists.first() != checklist
                    ) {
                        isChecklistPresented.value = true
                    }
                }
            }
        }

        item {

            MyList.Header("SHORTCUTS") {

                val isAddShortcutPresented = remember { mutableStateOf(false) }
                ShortcutFormSheet(isPresented = isAddShortcutPresented, editedShortcut = null)

                MyList.Header__RightIcon(
                    iconId = R.drawable.ic_round_add_24,
                    contentDescription = "New Shortcut"
                ) {
                    isAddShortcutPresented.value = true
                }
            }
        }

        val shortcuts = state.shortcuts
        itemsIndexed(shortcuts, key = { _, shortcut -> shortcut.id }) { _, shortcut ->

            val isShortcutEditPresented = remember { mutableStateOf(false) }
            ShortcutFormSheet(isPresented = isShortcutEditPresented, editedShortcut = shortcut)

            MyList.SectionItem(
                isFirst = shortcuts.first() == shortcut,
                isLast = shortcuts.last() == shortcut,
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
                    MyList.SectionItem_Button(
                        text = shortcut.name,
                        withDivider = shortcuts.first() != shortcut
                    ) {
                        performShortcutOrError(shortcut, context, errorDialog)
                    }
                }
            }
        }

        item {

            MyList.Header("SETTINGS")

            val isDayStartPresented = remember { mutableStateOf(false) }
            DayStartDialog(
                isPresented = isDayStartPresented,
                tabToolsVM = vm,
                tabToolsState = state,
            )

            MyList.SectionItem(isFirst = true, isLast = true, paddingTop = MyList.HEADER_BOTTOM_PADDING) {
                MyList.SectionItem_Button(
                    text = "Day Start",
                    withDivider = false,
                    rightView = {
                        Text(
                            state.dayStartNote,
                            modifier = Modifier.padding(horizontal = MyList.SECTION_ITEM_BUTTON_H_PADDING),
                            fontSize = 14.sp,
                            color = c.text,
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
                    .padding(top = MyListView.PADDING_SECTION_SECTION),
            )

            MyListView__SectionView(
                modifier = Modifier.padding(top = MyListView.PADDING_HEADER_SECTION)
            ) {

                MyListView__SectionView__ButtonView(
                    text = "Create",
                ) {
                    scope.launch {
                        val date = DateFormat.format("yyyyMMdd_HHmmss", Date())
                        launcherBackup.launch("timeto_${date}.json")
                    }
                }

                MyListView__SectionView__ButtonView(
                    text = "Restore",
                    withTopDivider = true
                ) {
                    scope.launch {
                        launcherRestore.launch("*/*")
                    }
                }

                val autoBackup = LocalAutoBackup.current
                if (autoBackup != null && isSDKQPlus()) {

                    val dateStr = autoBackup.lastCacheDate.value?.let { date ->
                        val calendar = Calendar.getInstance(Locale.ENGLISH)
                        val format = "d MMM, E HH:mm" // todo is24
                        calendar.timeInMillis = date.time
                        DateFormat.format(format, calendar).toString()
                    }

                    MyListView__SectionView__ButtonView(
                        text = "Auto Backup",
                        withTopDivider = true,
                        withArrow = true,
                        rightView = {
                            Text(
                                dateStr ?: "",
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .offset(),
                                fontSize = 14.sp,
                                color = c.text,
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

            MyList.Header("NOTIFICATIONS")

            MyList.SectionItem(isFirst = true, isLast = false, paddingTop = MyList.HEADER_BOTTOM_PADDING) {
                MyList.SectionItem_Button(text = "Time to Break", withDivider = false) {
                    context.startActivity(
                        Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            putExtra(Settings.EXTRA_CHANNEL_ID, NotificationCenter.channelTimeToBreak().id)
                        }
                    )
                }
            }

            MyList.SectionItem(isFirst = false, isLast = true) {
                MyList.SectionItem_Button(text = "Timer Overdue", withDivider = true) {
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

            MyList.SectionItem(isFirst = true, isLast = false, paddingTop = MyList.HEADER_BOTTOM_PADDING * 3) {
                MyList.SectionItem_Button(text = "Ask a Question", withDivider = false) {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("mailto:${state.feedbackEmail}?subject=${state.feedbackSubject}")
                        }
                    )
                }
            }

            MyList.SectionItem(isFirst = false, isLast = true) {
                MyList.SectionItem_Button(text = "Open Source", withDivider = true) {
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
