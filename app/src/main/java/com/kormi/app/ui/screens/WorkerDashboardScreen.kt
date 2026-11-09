package com.kormi.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(mainNavController: NavController, worker: Worker?) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (worker == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Navy)
        }
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 10.sp) },
                    selected = currentRoute == "worker_profile_tab",
                    onClick = { navController.navigate("worker_profile_tab") { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Navy, unselectedIconColor = TextSecondary, indicatorColor = Navy.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.DateRange, contentDescription = "Attendance") },
                    label = { Text("Attendance", fontSize = 10.sp) },
                    selected = currentRoute == "worker_attendance_tab",
                    onClick = { navController.navigate("worker_attendance_tab") { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Navy, unselectedIconColor = TextSecondary, indicatorColor = Navy.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Chat, contentDescription = "Messages") },
                    label = { Text("Messages", fontSize = 10.sp) },
                    selected = currentRoute == "worker_messages_tab",
                    onClick = { navController.navigate("worker_messages_tab") { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Navy, unselectedIconColor = TextSecondary, indicatorColor = Navy.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.LocationOn, contentDescription = "Location") },
                    label = { Text("Location", fontSize = 10.sp) },
                    selected = currentRoute == "worker_location_tab",
                    onClick = { navController.navigate("worker_location_tab") { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Navy, unselectedIconColor = TextSecondary, indicatorColor = Navy.copy(alpha = 0.1f))
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "worker_profile_tab",
            modifier = Modifier.padding(padding)
        ) {
            composable("worker_profile_tab") { WorkerProfileScreen(mainNavController, worker) }
            composable("worker_attendance_tab") { WorkerAttendanceScreen(worker) }
            composable("worker_messages_tab") { MessagesScreen(mainNavController, worker.workerId) }
            composable("worker_location_tab") { LocationScreen(mainNavController, worker, worker.role) }
        }
    }
}
