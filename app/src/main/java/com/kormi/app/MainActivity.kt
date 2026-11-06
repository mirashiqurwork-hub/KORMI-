package com.kormi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
            KormiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val context = LocalContext.current
                    val db = AppDatabase.getDatabase(context)
                    val scope = rememberCoroutineScope()
                    
                    var loggedInWorker by remember { mutableStateOf<Worker?>(null) }
                    var currentUserId by remember { mutableStateOf("") }

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") { 
                            LoginScreen(navController) { userId ->
                                currentUserId = userId
                                if (userId != "ADMIN") {
                                    scope.launch {
                                        loggedInWorker = db.workerDao().getWorkerByCard(userId)
                                    }
                                }
                            } 
                        }
                        composable("dashboard") { DashboardScreen(navController) }
                        
                        // Worker Dashboard (নতুন)
                        composable("worker_dashboard") { 
                            WorkerDashboardScreen(navController, loggedInWorker) 
                        }

                        composable("worker_details/{workerId}") { backStackEntry ->
                            val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
                            var selectedWorker by remember { mutableStateOf<Worker?>(null) }
                            LaunchedEffect(workerId) {
                                selectedWorker = db.workerDao().getWorkerByCard(workerId)
                            }
                            // অ্যাডমিন ভিউ এর জন্য আলাদা পেজ
                            WorkerProfileScreen(navController, selectedWorker)
                        }

                        composable("workers") { WorkersScreen(navController) }
                        composable("add_worker") { AddWorkerScreen(navController) }
                        composable("attendance") { AttendanceScreen(navController) }
                        composable("manpower") { ManpowerScreen(navController) }
                        composable("production") { ProductionScreen(navController) }
                        composable("admin") { AdminScreen(navController) }
                        composable("admin_passwords") { ManagePasswordsScreen(navController) }

                        composable("messages") { MessagesScreen(navController, currentUserId) }
                        composable("chat_detail/{senderId}/{receiverId}") { backStackEntry ->
                            val senderId = backStackEntry.arguments?.getString("senderId") ?: ""
                            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
                            ChatDetailScreen(navController, senderId, receiverId)
                        }
                        composable("location") { LocationScreen(navController) }
                        composable("notifications") { NotificationsScreen(navController) }
                    }
                }
            }
        }
    }
}
