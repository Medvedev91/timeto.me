package me.timeto.app

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.Manifest
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import me.timeto.app.ui.*
import kotlinx.coroutines.delay
import me.timeto.shared.*
import me.timeto.shared.vm.AppVM

var statusBarHeight = 0.dp

class MainActivity : ComponentActivity() {

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            batteryLevelOrNull = level * 100 / scale
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            isBatteryChargingOrNull = plugged != 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Remove system paddings including status and navigation bars.
        // Needs android:windowSoftInputMode="adjustNothing" in the manifest.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        statusBarHeight = getStatusBarHeight(this@MainActivity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            notificationsPermissionProcessing()

        setContent {

            val (vm, state) = rememberVM { AppVM() }
            val isLight = !isSystemInDarkTheme()

            MaterialTheme(colors = if (isLight) myLightColors() else myDarkColors()) {

                if (state.isAppReady) {

                    // c.transparent set the default background. WTF?!
                    // 0.004 based on Color(0x01......).alpha -> 0.003921569
                    val navigationBgColor = c.tabsBackground.copy(alpha = 0.004f).toArgb()
                    fun upNavigationUI() {
                        window.navigationBarColor = navigationBgColor
                        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = isLight
                    }
                    upNavigationUI() // Setting background and icons initially in xml. Here after tabs appear.

                    WrapperView.LayoutView {
                        TabsView()
                        UIListeners()
                        FullScreenListener(activity = this, onClose = ::upNavigationUI)
                    }

                    LaunchedEffect(Unit) {
                        if (isSDKQPlus()) {
                            autoBackupLastTimeCache.emit(AutoBackup.getLastDate()?.toUnixTime())
                            while (true) {
                                AutoBackup.dailyBackupIfNeeded()
                                delay(30_000L)
                            }
                        }
                    }

                    LaunchedEffect(Unit) {
                        scheduledNotificationsDataFlow
                            .onEachExIn(this) { notificationsData ->
                                NotificationCenter.cleanAllPushes()
                                notificationsData.forEach { scheduleNotification(it) }
                            }
                        // TRICK Run strictly after scheduledNotificationsDataFlow launch.
                        // TRICK Without delay the first event does not handled. 1L enough.
                        vm.onNotificationsPermissionReady(delayMls = 500L)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NotificationCenter.cleanAllPushes()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notificationsPermissionProcessing() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
            }
            shouldShowRequestPermissionRationale(
                Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                // todo
            }
            else -> {
                val requester = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // todo vm.onNotificationsPermissionReady()?
                }
                requester.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
private fun UIListeners() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        uiAlertFlow.onEachExIn(this) { data ->
            Dialog.show { layer ->
                AlertDialogView(data) { layer.close() }
            }
        }
        uiConfirmationFlow.onEachExIn(this) { data ->
            Dialog.show { layer ->
                ConfirmationDialogView(data) { layer.close() }
            }
        }
        uiShortcutFlow.onEachExIn(this) { shortcut ->
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(shortcut.uri)))
            } catch (e: ActivityNotFoundException) {
                showUiAlert("Invalid shortcut link")
            }
        }
        uiChecklistFlow.onEachExIn(this) { checklist ->
            Dialog.show { layer ->
                ChecklistDialogView(checklist) { layer.close() }
            }
        }
    }
}

private fun getStatusBarHeight(activity: Activity): Dp {
    val resources = activity.resources
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0)
        return pxToDp(resources.getDimensionPixelSize(resourceId)).dp
    reportApi("Invalid status_bar_height $resourceId")
    return 0.dp
}
