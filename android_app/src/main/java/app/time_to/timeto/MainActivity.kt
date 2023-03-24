package app.time_to.timeto

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.time_to.timeto.ui.*
import kotlinx.coroutines.delay
import timeto.shared.*
import timeto.shared.vm.AppVM

val LocalAutoBackup = compositionLocalOf<AutoBackup?> { throw Exception("LocalAutoBackup") }
val wrapperViewLayers = mutableStateListOf<WrapperView.Layer>()
var statusBarHeight = 0.dp

class MainActivity : ComponentActivity() {

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            batteryPrc = level * 100 / scale
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Remove system paddings including status and navigation bars.
        // Needs android:windowSoftInputMode="adjustNothing" in the manifest.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        statusBarHeight = getStatusBarHeight(this@MainActivity)

        setContent {

            val (vm, state) = rememberVM { AppVM() }
            val isDayOrNight = !isSystemInDarkTheme()

            MaterialTheme(
                colors = if (isDayOrNight) lightColors(primary = c.blue) else darkColors(primary = c.blue),
            ) {
                if (state.isAppReady) {

                    // c.transparent set the default background. WTF?!
                    // 0.004 based on Color(0x01......).alpha -> 0.003921569
                    val navigationBgColor = c.tabsBackground.copy(alpha = 0.004f).toArgb()
                    fun upNavigationUI() {
                        window.navigationBarColor = navigationBgColor
                        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = isDayOrNight
                    }
                    upNavigationUI() // Setting background and icons initially in xml. Here after tabs appear.

                    CompositionLocalProvider(
                        LocalAutoBackup provides if (isSDKQPlus()) remember { AutoBackup() } else null,
                    ) {

                        WrapperView.LayoutView {
                            TabsView()
                            UIListeners()
                            FullScreenListener(activity = this, onClose = ::upNavigationUI)
                        }

                        val autoBackup = LocalAutoBackup.current
                        LaunchedEffect(Unit) {
                            while (true) {
                                if (isSDKQPlus())
                                    autoBackup?.dailyBackupIfNeeded()
                                delay(30_000L)
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
    }

    override fun onResume() {
        super.onResume()
        NotificationCenter.cleanAllPushes()
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
