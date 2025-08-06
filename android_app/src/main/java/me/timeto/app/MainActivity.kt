package me.timeto.app

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.Manifest
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.main.MainScreen
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.NavigationFs
import me.timeto.app.ui.pxToDp
import me.timeto.app.ui.rememberVm
import me.timeto.shared.*
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.BatteryInfo
import me.timeto.shared.backups.AutoBackup
import me.timeto.shared.ShortcutPerformer
import me.timeto.shared.vm.app.AppVm

class MainActivity : ComponentActivity() {

    var statusBarHeightDp: Dp = 0.dp

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            BatteryInfo.emitLevel(level * 100 / scale)
            val plugged: Int = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            BatteryInfo.emitIsCharging(plugged != 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Remove system paddings including status and navigation bars.
        // Needs android:windowSoftInputMode="adjustResize" in the manifest.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        statusBarHeightDp = getStatusBarHeight(this@MainActivity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            notificationsPermissionProcessing()

        setContent {

            val (vm, state) = rememberVm {
                AppVm()
            }

            MaterialTheme(colors = darkColors()) {

                val backupMessage = state.backupMessage
                if (backupMessage != null)
                    BackupMessageView(backupMessage)
                else if (state.isAppReady) {

                    NavigationFs {
                        MainScreen()
                        ShortcutsListener()
                    }

                    LaunchedEffect(Unit) {
                        if (isSdkQPlus()) {
                            try {
                                AutoBackup.upLastTimeCache(AutoBackupAndroid.getLastTimeOrNull())
                            } catch (e: Throwable) {
                                reportApi("MainActivity AutoBackup.upLastTimeCache()\n$e")
                            }
                            while (true) {
                                AutoBackupAndroid.dailyBackupIfNeeded()
                                delay(30_000L)
                            }
                        }
                    }

                    LaunchedEffect(Unit) {
                        NotificationAlarm.flow.onEachExIn(this) { notifications ->
                            AlarmCenter.cancelAllAlarms()
                            NotificationCenter.cleanTimerPushes()
                            notifications.forEach {
                                AlarmCenter.scheduleNotification(it)
                            }
                        }
                        // TRICK Run strictly after scheduledNotificationsDataFlow launch.
                        // TRICK Without delay the first event does not handled. 1L enough.
                        vm.onNotificationsPermissionReady(delayMls = 500L)
                    }

                    LaunchedEffect(Unit) {
                        keepScreenOnStateFlow.onEachExIn(this) { keepScreenOn ->
                            val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            if (keepScreenOn) window.addFlags(flag)
                            else window.clearFlags(flag)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        NotificationCenter.cleanTimerPushes()

        /**
         * https://developer.android.com/develop/ui/views/layout/immersive#kotlin
         *
         * No systemBars(), because on Redmi the first touch opens navbar.
         *
         * Needs "android:windowLayoutInDisplayCutoutMode shortEdges" in manifest
         * to hide dark space on the top while WindowInsetsCompat.Type.statusBars()
         * like https://stackoverflow.com/q/72179274 in "2. Completely black...".
         * https://developer.android.com/develop/ui/views/layout/display-cutout
         */
        val statusBars = WindowInsetsCompat.Type.statusBars()
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(statusBars) // To show: controller.show(statusBars)
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
private fun BackupMessageView(
    message: String,
) {
    ZStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.black),
    ) {

        Text(
            text = message,
            modifier = Modifier
                .align(Alignment.Center),
            color = c.white,
        )
    }
}

@Composable
private fun ShortcutsListener() {
    val context = LocalContext.current
    val navigationFs = LocalNavigationFs.current
    LaunchedEffect(Unit) {
        ShortcutPerformer.flow.onEachExIn(this) { shortcutDb ->
            try {
                val uri: String = shortcutDb.uri
                if (uri.startsWith(ShortcutDb.ANDROID_PACKAGE_PREFIX)) {
                    val androidPackage = uri.replaceFirst(ShortcutDb.ANDROID_PACKAGE_PREFIX, "")
                    val intent: Intent? = context.packageManager.getLaunchIntentForPackage(androidPackage)
                    if (intent == null)
                        navigationFs.alert("App package not found")
                    else
                        context.startActivity(intent)
                } else {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(shortcutDb.uri)))
                }
            } catch (_: ActivityNotFoundException) {
                navigationFs.alert("Invalid shortcut link")
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
