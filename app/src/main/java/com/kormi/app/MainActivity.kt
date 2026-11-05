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

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") { LoginScreen(navController) }
                        composable("dashboard") { DashboardScreen(navController) }
                        
                        composable("profile/{workerId}") { backStackEntry ->
                            val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
                            LaunchedEffect(workerId) {
                                scope.launch {
                                    loggedInWorker = db.workerDao().getWorkerByCard(workerId)
                                }
                            }
                            WorkerProfileScreen(navController, loggedInWorker)
                        }

                        composable("workers") { WorkersScreen(navController) }
                        composable("attendance") { AttendanceScreen(navController) }
                        composable("manpower") { ManpowerScreen(navController) }
                        composable("production") { ProductionScreen(navController) }
                        composable("messages") { MessagesScreen(navController) }
                        composable("location") { LocationScreen(navController) }
                        composable("notifications") { NotificationsScreen(navController) }
                        composable("admin") { AdminScreen(navController) }
                    }
                }
            }
        }
    }
}
