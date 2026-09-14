package com.swasthyasathi.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swasthyasathi.app.data.model.DEFAULT_INDIAN_CITIES
import com.swasthyasathi.app.ui.components.ConsumerProfileDialog
import com.swasthyasathi.app.ui.components.LocationSearchDialog
import com.swasthyasathi.app.ui.components.SwasthyaLogo
import com.swasthyasathi.app.ui.components.bounceClick
import com.swasthyasathi.app.ui.screens.*
import com.swasthyasathi.app.ui.theme.*
import com.swasthyasathi.app.viewmodel.HealthViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Login : Screen("login", "Login", Icons.Default.Person)
    object Overview : Screen("overview", "Overview", Icons.Default.Favorite)
    object Climate : Screen("climate", "Climate", Icons.Default.Cloud)
    object Hydrate : Screen("hydrate", "Hydrate", Icons.Default.WaterDrop)
    object Shelters : Screen("shelters", "Shelters", Icons.Default.LocationOn)
    object Triage : Screen("triage", "Triage", Icons.Default.Dashboard)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Overview.route

    val isChatOpen by viewModel.isChatOpen.collectAsState()
    val isSosOpen by viewModel.isSosOpen.collectAsState()
    val isProfileDrawerOpen by viewModel.isProfileDrawerOpen.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()

    val isLocationSearchOpen by viewModel.isLocationSearchOpen.collectAsState()
    val isLocatingGps by viewModel.isLocatingGps.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectRealLocation(context)
        }
    }

    val navItems = listOf(
        Screen.Overview,
        Screen.Climate,
        Screen.Hydrate,
        Screen.Shelters,
        Screen.Triage
    )

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            if (currentRoute != Screen.Login.route) {
                Surface(
                    color = SurfaceContainerLowest,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, SurfaceContainerHigh.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(
                                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                                )
                            )
                    ) {
                        // Main Brand Bar (Netflix-style bold typography)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Netflix-style Brand Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.Overview.route) {
                                        popUpTo(Screen.Overview.route) { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                            ) {
                                Surface(
                                    modifier = Modifier.size(38.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = EmeraldContainer,
                                    border = BorderStroke(1.5.dp, EmeraldPrimary)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        SwasthyaLogo(size = 30.dp)
                                    }
                                }
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "SWASTHYASATHI",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = (-0.5).sp
                                            ),
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = "-AI",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = SecondaryCoral
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(RiskLow)
                                        )
                                        Text(
                                            text = "LIVE EDGE SYNC",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Right Actions: SOS & Consumer Profile Avatar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.setSosOpen(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryCoral),
                                    shape = RoundedCornerShape(100.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Icon(Icons.Default.Emergency, contentDescription = "SOS", modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SOS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }

                                // Consumer Profile Avatar Button (Top Right, 44dp Touch Target)
                                Surface(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .clickable { viewModel.setProfileDrawerOpen(true) },
                                    shape = CircleShape,
                                    color = EmeraldContainer,
                                    border = BorderStroke(1.5.dp, EmeraldPrimary)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Consumer Profile",
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Sub-bar: Location Selector with Search & GPS
                        Surface(
                            color = SurfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .clickable { viewModel.setLocationSearchOpen(true) }
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = RiskHigh, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "${currentCity.name}, ${currentCity.state}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = OnSurface,
                                        maxLines = 1
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                                }

                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = if (isLocatingGps) EmeraldContainer else SurfaceContainerHighest,
                                    modifier = Modifier.clickable {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                        viewModel.detectRealLocation(context)
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        if (isLocatingGps) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                color = EmeraldPrimary,
                                                strokeWidth = 1.5.dp
                                            )
                                        } else {
                                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(12.dp))
                                        }
                                        Text(
                                            text = if (isLocatingGps) "LOCKING GPS..." else "GPS DETECT",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = PrimaryTeal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (currentRoute != Screen.Login.route) {
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
                                        popUpTo(Screen.Overview.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryTeal,
                                selectedTextColor = PrimaryTeal,
                                indicatorColor = PrimaryTeal.copy(alpha = 0.12f),
                                unselectedIconColor = OnSurfaceVariant,
                                unselectedTextColor = OnSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Overview.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Overview.route) {
                OverviewScreen(
                    viewModel = viewModel,
                    onNavigateToShelters = { navController.navigate(Screen.Shelters.route) },
                    onNavigateToClimate = { navController.navigate(Screen.Climate.route) },
                    onNavigateToHydrate = { navController.navigate(Screen.Hydrate.route) },
                    onNavigateToProfile = { viewModel.setProfileDrawerOpen(true) }
                )
            }
            composable(Screen.Climate.route) {
                ClimateScreen(viewModel = viewModel)
            }
            composable(Screen.Hydrate.route) {
                HydrateScreen(viewModel = viewModel)
            }
            composable(Screen.Shelters.route) {
                SheltersScreen(viewModel = viewModel)
            }
            composable(Screen.Triage.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToProfile = { viewModel.setProfileDrawerOpen(true) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Global Overlay Consumer Profile Dialog
        if (isProfileDrawerOpen) {
            ConsumerProfileDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setProfileDrawerOpen(false) },
                onNavigateToFullProfile = {
                    viewModel.setProfileDrawerOpen(false)
                    navController.navigate(Screen.Profile.route)
                }
            )
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

        // Global Location Search & GPS Dialog
        if (isLocationSearchOpen) {
            LocationSearchDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setLocationSearchOpen(false) }
            )
        }
    }
}
