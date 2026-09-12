package com.swasthyasathi.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swasthyasathi.app.ui.screens.*
import com.swasthyasathi.app.ui.theme.PrimaryTeal
import com.swasthyasathi.app.ui.theme.SurfaceContainerLowest
import com.swasthyasathi.app.viewmodel.HealthViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Overview : Screen("overview", "Overview", Icons.Default.Info)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Privacy : Screen("privacy", "Privacy", Icons.Default.Lock)
}

@Composable
fun MainNavigation(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val isChatOpen by viewModel.isChatOpen.collectAsState()
    val isSosOpen by viewModel.isSosOpen.collectAsState()

    val navItems = listOf(
        Screen.Dashboard,
        Screen.Overview,
        Screen.Profile,
        Screen.Privacy
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceContainerLowest,
                tonalElevation = 6.dp
            ) {
                navItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryTeal,
                            selectedTextColor = PrimaryTeal,
                            indicatorColor = PrimaryTeal.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.Overview.route) {
                OverviewScreen(
                    onLaunchDashboard = { navController.navigate(Screen.Dashboard.route) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Privacy.route) {
                PrivacyScreen()
            }
        }

        // Global Overlay AI Sheet
        if (isChatOpen) {
            AiAssistantSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.setChatOpen(false) }
            )
        }

        // Global Overlay SOS Dialog
        if (isSosOpen) {
            SosDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setSosOpen(false) }
            )
        }
    }
}
