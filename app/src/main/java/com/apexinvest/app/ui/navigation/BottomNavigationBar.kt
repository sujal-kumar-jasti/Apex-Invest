package com.apexinvest.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("main_pager", Icons.Default.Home, "Home")
    object Portfolio : BottomNavItem("main_pager", Icons.Default.PieChart, "Holdings")
    object Ideas : BottomNavItem("main_pager", Icons.Default.Lightbulb, "Ideas")
    object Predict : BottomNavItem("main_pager", Icons.Default.Analytics, "Predict")

}

@Composable
fun ApexBottomBar(navController: NavController) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Portfolio, BottomNavItem.Ideas, BottomNavItem.Predict)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (currentRoute in items.map { it.route }) {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    selected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate("main_pager") {
                                launchSingleTop = true
                            }

                        }
                    }
                )
            }
        }
    }
}