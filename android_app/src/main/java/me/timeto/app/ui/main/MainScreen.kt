package me.timeto.app.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.activities.ActivitiesScreen
import me.timeto.app.ui.home.HomeScreen
import me.timeto.app.ui.navigation.NavigationScreen
import me.timeto.app.ui.settings.SettingsScreen
import me.timeto.app.ui.tasks.tab.TasksTabView

@Composable
fun MainScreen() {

    val tab = remember {
        mutableStateOf(MainTabEnum.home)
    }

    VStack {

        ZStack(
            modifier = Modifier
                .weight(1f),
        ) {
            when (tab.value) {
                MainTabEnum.home -> {
                    NavigationScreen {
                        HomeScreen()
                    }
                }
                MainTabEnum.activities -> {
                    NavigationScreen {
                        ActivitiesScreen(
                            onClose = {
                                tab.value = MainTabEnum.home
                            },
                        )
                    }
                }
                MainTabEnum.tasks -> {
                    NavigationScreen {
                        TasksTabView(
                            onClose = {
                                tab.value = MainTabEnum.home
                            },
                        )
                    }
                }
                MainTabEnum.settings -> {
                    NavigationScreen {
                        SettingsScreen(
                            onClose = {
                                tab.value = MainTabEnum.home
                            },
                        )
                    }
                }
            }
        }

        MainTabsView(
            tab = tab.value,
            onTabChanged = { newTab ->
                tab.value = newTab
            },
        )
    }
}
