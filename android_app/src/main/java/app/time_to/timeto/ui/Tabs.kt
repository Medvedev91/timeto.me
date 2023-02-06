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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.time_to.timeto.MyException
import app.time_to.timeto.R
import app.time_to.timeto.rememberVM
import timeto.shared.vm.TabsVM

// todo
// https://stackoverflow.com/q/67059823
var globalNav: NavHostController? = null

val LocalTabsHeight = compositionLocalOf<Dp> { throw MyException("LocalTabsHeight") }

@Composable
fun Tabs() {
    val navController = rememberNavController()
    globalNav = navController
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

        CompositionLocalProvider(
            LocalTabsHeight provides navHeight
        ) {
            NavHost(
                navController,
                modifier = Modifier
                    // Otherwise on IME hide we see another background)
                    .background(c.background)
                    .padding(bottom = navHeight),
                startDestination = TabItem.Timer.route
            ) {
                composable(TabItem.Timer.route) {
                    TabTimerView()
                }
                composable(TabItem.Tasks.route) {
                    TabTasksView()
                }
                composable(TabItem.Tools.route) {
                    TabToolsView()
                }
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
    object Tools : TabItem("Tools", R.drawable.sf_gearshape_large_heavy, "tools")
}

@Composable
private fun BottomNavigation(
    navController: NavController,
) {

    val items = listOf(
        TabItem.Timer,
        TabItem.Tasks,
        TabItem.Tools,
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
