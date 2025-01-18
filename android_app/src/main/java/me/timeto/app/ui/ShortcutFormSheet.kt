package me.timeto.app.ui

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.vm.ShortcutFormVm

@Composable
fun ShortcutFormSheet(
    layer: WrapperView.Layer,
    editedShortcut: ShortcutDb?,
) {

    val (vm, state) = rememberVm(editedShortcut) { ShortcutFormVm(editedShortcut) }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg)
    ) {

        val scrollState = rememberScrollState()

        Sheet.HeaderViewOld(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.headerDoneText,
            isDoneEnabled = state.isHeaderDoneEnabled,
            scrollState = scrollState,
        ) {
            vm.save {
                layer.close()
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            MyListView__Padding__SectionSection()

            MyListView__HeaderView(
                title = state.inputNameHeader,
            )

            MyListView__Padding__HeaderSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
            ) {
                MyListView__ItemView__TextInputView(
                    placeholder = state.inputNamePlaceholder,
                    text = state.inputNameValue,
                    onTextChanged = { newText -> vm.setInputNameValue(newText) },
                )
            }

            MyListView__HeaderView(
                title = state.inputUriHeader,
                Modifier.padding(top = 30.dp)
            )

            MyListView__Padding__HeaderSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
            ) {
                MyListView__ItemView__TextInputView(
                    placeholder = state.inputUriPlaceholder,
                    text = state.inputUriValue,
                    onTextChanged = { newText -> vm.setInputUriValue(newText) },
                )
            }

            MyListView__HeaderView(
                title = "EXAMPLES",
                Modifier.padding(top = 60.dp)
            )

            MyListView__Padding__HeaderSection()

            shortcutExamples.forEach { example ->
                val isFirst = shortcutExamples.first() == example
                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = shortcutExamples.last() == example,
                    withTopDivider = !isFirst,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = example.name,
                        rightView = {
                            Row(
                                modifier = Modifier.padding(end = 14.dp)
                            ) {
                                Text(
                                    example.hint,
                                    fontSize = 14.sp,
                                    color = c.text,
                                )
                                AnimatedVisibility(
                                    visible = state.inputUriValue == example.uri,
                                ) {
                                    Icon(
                                        painterResource(id = R.drawable.sf_checkmark_medium_medium),
                                        "Selected",
                                        tint = c.green,
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .size(18.dp)
                                            .alpha(0.8f)
                                            .clip(roundedShape)
                                            .padding(3.dp)
                                    )
                                }
                            }
                        }
                    ) {
                        vm.setInputNameValue(example.name)
                        vm.setInputUriValue(example.uri)
                        keyboardController?.hide()
                    }
                }
            }

            MyListView__Padding__SectionSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
            ) {
                MyListView__ItemView__ButtonView(
                    text = "Apps",
                ) {
                    Sheet.show { layer ->
                        AppsListSheet(
                            layer = layer,
                            onAppSelected = { shortcutApp ->
                                vm.setInputNameValue(shortcutApp.name)
                                vm.setAndroidPackage(shortcutApp.androidPackage)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppsListSheet(
    layer: WrapperView.Layer,
    onAppSelected: (ShortcutApp) -> Unit,
) {

    val allApps = remember { mutableStateOf<List<ShortcutApp>>(emptyList()) }
    LaunchedEffect(Unit) {
        allApps.value = getAllApps()
    }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg)
    ) {

        Sheet__HeaderView(
            title = "Apps",
            scrollState = null,
            bgColor = c.sheetBg,
        )

        val scrollState = rememberScrollState()

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
        ) {

            allApps.value.forEach { shortcutApp ->

                VStack(
                    modifier = Modifier
                        .clickable {
                            onAppSelected(shortcutApp)
                            layer.close()
                        }
                        .padding(horizontal = H_PADDING),
                ) {

                    Text(
                        text = shortcutApp.name,
                        modifier = Modifier
                            .padding(vertical = 8.dp),
                        color = c.text,
                    )

                    DividerFg()
                }
            }

            HStack(modifier = Modifier.navigationBarsPadding()) {}
        }
    }
}

private fun getAllApps(): List<ShortcutApp> {
    val packageManager = App.instance.packageManager
    val packagesInfo = packageManager.getInstalledPackages(0)
    return packagesInfo
        // Ignore system apps
        // .filter { (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
        .map { packageInfo ->
            ShortcutApp(
                name = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                androidPackage = packageInfo.applicationInfo.packageName,
                icon = packageInfo.applicationInfo.loadIcon(packageManager),
            )
        }
        .sortedBy { it.name.lowercase() }
}

private class ShortcutApp(
    val name: String,
    val androidPackage: String,
    val icon: Drawable,
)

private val shortcutExamples = listOf(
    ShortcutExample(name = "10-Minute Meditation", hint = "Youtube", uri = "https://www.youtube.com/watch?v=O-6f5wQXSu8"),
    ShortcutExample(name = "Play a Song 😈", hint = "Music App", uri = "https://music.youtube.com/watch?v=ikFFVfObwss&feature=share"),
)

private class ShortcutExample(
    val name: String,
    val hint: String,
    val uri: String,
)
