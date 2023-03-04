package app.time_to.timeto

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.time_to.timeto.ui.*
import kotlinx.coroutines.delay
import timeto.shared.*
import timeto.shared.vm.AppVM

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

                    CompositionLocalProvider(
                        LocalAutoBackup provides if (isSDKQPlus()) remember { AutoBackup() } else null,
                        LocalWrapperViewLayers provides remember { mutableStateListOf() }
                    ) {

                        WrapperView.LayoutView {
                            TabsView()
                            UIListeners()
                            FullScreenView(activity = this, onClose = ::upNavigationUI)
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
    val layers = LocalWrapperViewLayers.current
    val checklistBgColor = c.background
    LaunchedEffect(Unit) {
        uiAlertFlow.onEachExIn(this) { data ->
            MyDialog.show(layers = layers) { layer ->
                AlertDialogView(data) { layer.onClose(layer) }
            }
        }
        uiConfirmationFlow.onEachExIn(this) { data ->
            showConfirmation(layers = layers, data = data)
        }
        uiShortcutFlow.onEachExIn(this) { shortcut ->
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(shortcut.uri)))
            } catch (e: ActivityNotFoundException) {
                showUiAlert("Invalid shortcut link")
            }
        }
        uiChecklistFlow.onEachExIn(this) { checklist ->
            ChecklistDialog__show(
                checklist = checklist,
                allLayers = layers,
                backgroundColor = checklistBgColor,
            )
        }
    }
}

private fun showConfirmation(
    layers: MutableList<WrapperView__LayerData>,
    data: UIConfirmationData,
) {
    MyDialog.show(
        layers = layers,
    ) { layer ->

        Column(
            modifier = Modifier
                .background(c.background2)
                .padding(20.dp)
        ) {

            Text(
                text = data.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 5.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Text(
                    "Cancel",
                    color = c.textSecondary,
                    modifier = Modifier
                        .padding(end = 11.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { layer.onClose(layer) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                MyButton(data.buttonText, true, if (data.isRed) c.red else c.blue) {
                    data.onConfirm()
                    layer.onClose(layer)
                }
            }
        }
    }
}
