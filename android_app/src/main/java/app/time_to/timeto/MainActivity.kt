package app.time_to.timeto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.time_to.timeto.ui.*
import kotlinx.coroutines.delay
import timeto.shared.*
import timeto.shared.db.*
import timeto.shared.vm.AppVM

val LocalTriggersDialogManager = compositionLocalOf<TriggersView__DialogManager> { throw MyException("LocalTriggersDialogManager") }
val LocalAutoBackup = compositionLocalOf<AutoBackup?> { throw MyException("LocalAutoBackup") }
val LocalWrapperViewLayers = compositionLocalOf<MutableList<WrapperView__LayerData>> { throw MyException("LocalWrapperViewLayers") }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove system paddings including status and navigation bars.
        // Needs android:windowSoftInputMode="adjustNothing" in the manifest.
        WindowCompat.setDecorFitsSystemWindows(window, false)

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

                    MyLocalProvider {

                        WrapperView {

                            FullScreenView(activity = this, onClose = ::upNavigationUI)

                            UIListeners()

                            Surface(Modifier.statusBarsPadding()) {

                                Tabs()

                                val localTriggersDialogManager = LocalTriggersDialogManager.current
                                localTriggersDialogManager.checklist.value?.let { checklist ->
                                    ChecklistDialog(checklist, localTriggersDialogManager.checklistIsPresented)
                                }
                                ListenNewIntervalForTriggers()
                            }
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
    val layers = LocalWrapperViewLayers.current
    val alertDialogBgColor = c.background2
    LaunchedEffect(Unit) {
        uiConfirmationFlow.onEachExIn(this) { data ->
            MyDialog__showConfirmation(
                allLayers = layers,
                data = data,
                backgroundColor = alertDialogBgColor,
            )
        }
        uiAlertFlow.onEachExIn(this) { data ->
            MyDialog__showAlert(
                allLayers = layers,
                data = data,
                backgroundColor = alertDialogBgColor,
            )
        }
    }
}

@Composable
private fun MyLocalProvider(
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(
        LocalTriggersDialogManager provides remember { TriggersView__DialogManager() },
        LocalAutoBackup provides if (isSDKQPlus()) remember { AutoBackup(scope) } else null,
        LocalWrapperViewLayers provides remember { mutableStateListOf() }
    ) {
        content()
    }
}

@Composable
fun ListenNewIntervalForTriggers() {
    val context = LocalContext.current

    val localTriggersDialogManager = LocalTriggersDialogManager.current
    val lastIntervalLive = IntervalModel.getLastOneOrNullFlow().collectAsState(null).value
    LaunchedEffect(lastIntervalLive?.id) {
        val lastInterval = lastIntervalLive ?: return@LaunchedEffect
        if ((lastInterval.id + 3) < time())
            return@LaunchedEffect

        // #GD AUTOSTART_TRIGGERS
        val stringToCheckTriggers = lastInterval.note ?: lastInterval.getActivityDI().name

        val trigger = TextFeatures.parse(stringToCheckTriggers)
            .triggers.firstOrNull() ?: return@LaunchedEffect

        localTriggersDialogManager.show(trigger, context)
    }
}
