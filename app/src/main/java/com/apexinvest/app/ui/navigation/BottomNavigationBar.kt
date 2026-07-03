package com.apexinvest.app.ui.navigation

<<<<<<< HEAD
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

// 1. UPDATED DATA CLASS: Added an optional badgeCount for future notifications
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String, val badgeCount: Int = 0) {
    object Home : BottomNavItem("tab_home", Icons.Default.Home, "Home")
    object Portfolio : BottomNavItem("tab_holdings", Icons.Default.PieChart, "Holdings")
    object Ideas : BottomNavItem("tab_ideas", Icons.Default.Lightbulb, "Ideas")
    object Predict : BottomNavItem("tab_predict", Icons.Default.Analytics, "Predict")
}

// 2. STATIC LIST & SHAPES: Lifted to prevent memory reallocation
private val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Portfolio,
    BottomNavItem.Ideas,
    BottomNavItem.Predict
)
private val BottomBarShape = RoundedCornerShape(42.dp)

@Composable
fun ApexBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier // 🌟 Crucial for allowing the parent Box to align it to BottomCenter
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // CACHED VISIBILITY CHECK: Only shows the floating bar if we are on a primary tab screen
    val showBottomBar = remember(currentRoute) {
        currentRoute != null && bottomNavItems.any { it.route == currentRoute }
    }

    if (showBottomBar) {
        val isDark = isSystemInDarkTheme()

        // MEMOIZED COLORS: Caching UI states to prevent allocation lag during animations
        val purpleAccent = remember(isDark) { if (isDark) Color(0xFF9E86FF) else Color(0xFF673AB7) }
        val shadowSize = remember(isDark) { if (isDark) 24.dp else 16.dp }
        val bgColor = remember(isDark) { if (isDark) 0.85f else 0.95f }
        val borderColor = remember(isDark) { if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f) }

        // CACHED HIERARCHY: O(1) lookup map for active states
        val hierarchyRoutes = remember(currentDestination) {
            currentDestination?.hierarchy?.mapNotNull { it.route }?.toSet() ?: emptySet()
        }

        // 3. THE CUSTOM FLOATING UI
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp) // Creates the floating gap from the edges
                .height(80.dp)
                .shadow(shadowSize, BottomBarShape, spotColor = purpleAccent.copy(alpha = 0.3f))
                .clip(BottomBarShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = bgColor))
                .border(1.5.dp, borderColor, BottomBarShape)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = hierarchyRoutes.contains(item.route)

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                indication = null, // Removes the default square ripple for a cleaner look
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    if (!isSelected) {
                                        navController.navigate(item.route) {
                                            // The magic standard for bottom navigation routing
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) purpleAccent.copy(alpha = 0.15f) else Color.Transparent)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (item.badgeCount > 0) {
                                        Badge(containerColor = purpleAccent, contentColor = Color.White) {
                                            Text(item.badgeCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(if (isSelected) 32.dp else 28.dp),
                                    tint = if (isSelected) purpleAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
            }
        }
    }
}