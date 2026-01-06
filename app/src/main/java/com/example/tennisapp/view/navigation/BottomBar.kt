package com.example.tennisapp.view.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

private data class BottomItem(
    val screen: Screen,
    val icon: ImageVector
)

@Composable
fun BottomBar(
    navController: NavController,
    visible: Boolean
) {
    if (!visible) return

    val items = listOf(
        BottomItem(Screen.Welcome, Icons.Default.Home),
        BottomItem(Screen.Live, Icons.Default.PlayArrow),
        BottomItem(Screen.Results, Icons.Default.BarChart),
        BottomItem(Screen.Summary, Icons.Default.List)
    )

    NavigationBar {
        val backStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry.value?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Welcome.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.screen.label) },
                label = { Text(item.screen.label) }
            )
        }
    }
}