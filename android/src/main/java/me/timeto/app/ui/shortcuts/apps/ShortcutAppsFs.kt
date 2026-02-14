package me.timeto.app.ui.shortcuts.apps

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.App
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.Divider
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.form__itemMinHeight
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationLayer

@Composable
fun ShortcutAppsFs(
    onAppSelected: (ShortcutApp) -> Unit,
) {

    val navigationLayer = LocalNavigationLayer.current

    val apps = remember {
        mutableStateOf(emptyList<ShortcutApp>())
    }
    LaunchedEffect(Unit) {
        apps.value = getApps()
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = "Apps",
            scrollState = scrollState,
            actionButton = null,
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            )
        )

        val appsValue: List<ShortcutApp> = apps.value
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
            contentPadding = PaddingValues(bottom = 25.dp),
        ) {
            appsValue.forEach { app ->
                item {
                    ZStack(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = form__itemMinHeight)
                            .clickable {
                                onAppSelected(app)
                                navigationLayer.close()
                            },
                    ) {
                        if (appsValue.first() != app) {
                            Divider(
                                modifier = Modifier
                                    .padding(start = H_PADDING)
                                    .align(Alignment.TopEnd)
                            )
                        }
                        Text(
                            text = app.name,
                            modifier = Modifier
                                .padding(horizontal = H_PADDING)
                                .padding(vertical = 4.dp)
                                .align(Alignment.CenterStart),
                            color = c.text,
                        )
                    }
                }
            }
        }
    }
}

///

private fun getApps(): List<ShortcutApp> {
    val packageManager = App.instance.packageManager
    val packagesInfo: List<PackageInfo> =
        packageManager.getInstalledPackages(0)
    return packagesInfo
        // Ignore system apps
        // .filter { (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
        .map { packageInfo ->
            val applicationInfo: ApplicationInfo = packageInfo.applicationInfo!!
            ShortcutApp(
                name = applicationInfo.loadLabel(packageManager).toString(),
                androidPackage = applicationInfo.packageName,
                icon = applicationInfo.loadIcon(packageManager),
            )
        }
        .sortedBy { shortcutApp ->
            shortcutApp.name.lowercase()
        }
}
