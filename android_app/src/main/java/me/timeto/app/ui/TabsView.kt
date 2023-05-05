package app.time_to.timeto.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.time_to.timeto.*
import app.time_to.timeto.R
import timeto.shared.FullScreenUI
import timeto.shared.launchEx
import timeto.shared.vm.TabsVM

val bottomNavigationHeight = 56.dp

@Composable
fun TabsView() {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier
            .background(c.tabsBackground)
            .navigationBarsPadding(),
        bottomBar = {
            Box {
                BottomNavigation(navController = navController)
                Divider(
                    thickness = 0.5.dp,
                    color = c.dividerBackground2,
                    modifier = Modifier.alpha(0.8f)
                )
            }
        }
    ) { navPadding ->

        val navHeight = navPadding.calculateBottomPadding()

        NavHost(
            navController,
            modifier = Modifier
                .background(c.background) // Fix on IME hide another background
                .padding(top = statusBarHeight, bottom = navHeight),
            startDestination = TabItem.Timer.route
        ) {
            composable(TabItem.Timer.route) {
                TabTimerView()
            }
            composable(TabItem.Tasks.route) {
                TabTasksView(
                    modifier = Modifier,
                    onTaskStarted = {
                        scope.launchEx {
                            navController.navigate(TabItem.Timer.route) {
                                popUpTo(0)
                            }
                        }
                    }
                )
            }
            composable(TabItem.Focus.route) {
                // todo
                Text("")
            }
        }
    }
}

sealed class TabItem(
    val title: String,
    val icon: Int,
    val route: String,
) {
    object Timer : TabItem("Timer", R.drawable.sf_timer_large_bold, "timer")
    object Tasks : TabItem("Tasks", R.drawable.sf_tray_full_medium_semibold, "tasks")
    object Focus : TabItem("Focus", R.drawable.sf_timelapse_small_black, "focus")
}

@Composable
private fun BottomNavigation(
    navController: NavController,
) {

    val items = listOf(
        TabItem.Timer,
        TabItem.Tasks,
        TabItem.Focus,
    )

    val (_, state) = rememberVM { TabsVM() }

    androidx.compose.material.BottomNavigation(
        backgroundColor = c.tabsBackground
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->

            val icon = @Composable {
                Icon(
                    painterResource(id = item.icon),
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(bottom = 2.dp)
                        .rotate(if (item == TabItem.Focus) 90f else 0f)
                )
            }

            BottomNavigationItem(
                icon = {
                    BadgedBox(
                        badge = {

                            Column {

                                val todayBadge = state.todayBadge

                                AnimatedVisibility(
                                    visible = item == TabItem.Tasks && todayBadge > 0,
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .offset(y = 2.dp)
                                            .size(18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {

                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(99.dp))
                                                .background(c.tabsBackground)
                                        )

                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .padding(1.dp)
                                                .clip(RoundedCornerShape(99.dp))
                                                .background(c.blue)
                                        )

                                        Text(
                                            when {
                                                todayBadge < 1 -> "1" // Otherwise, on remove the last, while animation we see 0
                                                todayBadge > 99 -> "..."
                                                else -> todayBadge.toString()
                                            },
                                            style = TextStyle(
                                                color = c.white,
                                                fontSize = if (todayBadge < 10) 10.sp else 9.sp,
                                                fontWeight = FontWeight.W400,
                                                fontFamily = FontFamily.Default,
                                                letterSpacing = (-0.3).sp
                                            ),
                                            maxLines = 1,
                                            modifier = Modifier.offset(y = (-0.2).dp)
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        icon()
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 10.sp
                    )
                },
                selectedContentColor = c.blue,
                unselectedContentColor = c.tabsText,
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                onClick = {

                    if (item == TabItem.Focus) {
                        FullScreenUI.open()
                        return@BottomNavigationItem
                    }

                    if (item.route == TabItem.Tasks.route)
                        setTodayFolder?.invoke()

                    navController.navigate(item.route) {

                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
