package com.kormi.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Worker
import com.kormi.app.ui.screens.*
import com.kormi.app.ui.theme.KormiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val sharedPref = context.getSharedPreferences("KormiPrefs", Context.MODE_PRIVATE)
            var currentFontScale by remember { mutableStateOf(sharedPref.getFloat("fontScale", 1.0f)) }

            KormiTheme(fontScale = currentFontScale) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val db = AppDatabase.getDatabase(context)
                    val scope = rememberCoroutineScope()
                    
                    val savedUserId = sharedPref.getString("userId", "") ?: ""
                    val savedRole = sharedPref.getString("role", "") ?: ""

                    var loggedInWorker by remember { mutableStateOf<Worker?>(null) }
                    var currentUserId by remember { mutableStateOf(savedUserId) }
                    var currentUserRole by remember { mutableStateOf(savedRole) }

                    val startDestination = if (savedUserId.isNotEmpty()) {
                        if (savedRole == "Worker") "worker_dashboard" else "dashboard"
                    } else { "login" }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") { 
                            LoginScreen(navController) { userId, role ->
                                currentUserId = userId
                                currentUserRole = role
                                scope.launch {
                                    loggedInWorker = if (role != "ADMIN") {
                                        db.workerDao().getWorkerByCard(userId)
                                    } else {
                                        db.workerDao().getWorkerByCard("ADMIN")
                                    }
                                }
                            } 
                        }
                        composable("dashboard") { 
                            DashboardScreen(
                                navController = navController, 
                                userRole = currentUserRole, 
                                userName = if (currentUserRole == "ADMIN") "Admin" else (loggedInWorker?.fullName ?: "User"), 
                                currentUserId = currentUserId, 
                                fontScale = currentFontScale
                            ) { newScale ->
                                currentFontScale = newScale
                                sharedPref.edit().putFloat("fontScale", newScale).apply()
                            } 
                        }
                        composable("worker_dashboard") {
                            WorkerDashboardScreen(navController = navController, workerId = currentUserId)
                        }
                        composable("workers") { WorkersScreen(navController) }
                        composable("add_worker") { AddWorkerScreen(navController) }
                        composable("attendance") { AttendanceScreen(navController) }
                        composable("manpower") { ManpowerScreen(navController) }
                        composable("production") { ProductionScreen(navController) }
                        composable("breakdown") { BreakdownScreen(navController) }
                        composable("messages") { MessagesScreen(navController, currentUserId) }
                        composable("location") { LocationScreen(navController) }
                        composable("notifications") { NotificationsScreen(navController) }
                        composable("admin") { AdminScreen(navController) }

                        // Fixed Chat Detail Route with required parameters
                        composable(
                            route = "chat_detail/{recipientId}/{recipientName}",
                            arguments = listOf(
                                navArgument("recipientId") { type = NavType.StringType },
                                navArgument("recipientName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""
                            val recipientName = backStackEntry.arguments?.getString("recipientName") ?: ""
                            ChatDetailScreen(
                                navController = navController,
                                currentUserId = currentUserId,
                                recipientId = recipientId,
                                recipientName = recipientName
                            )
                        }

                        // Fixed Group Chat Detail Route with required parameters
                        composable(
                            route = "group_chat_detail/{groupId}/{groupName}",
                            arguments = listOf(
                                navArgument("groupId") { type = NavType.StringType },
                                navArgument("groupName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                            val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
                            GroupChatDetailScreen(
                                navController = navController,
                                currentUserId = currentUserId,
                                groupId = groupId,
                                groupName = groupName
                            )
                        }
                    }
                }
            }
        }
    }
}
