package com.kormi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kormi.app.ui.screens.*
import com.kormi.app.ui.theme.KormiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KormiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "login" // অ্যাপ খুললে প্রথমে লগইন স্ক্রিন আসবে
                    ) {
                        composable("login") { LoginScreen(navController) }
                        composable("dashboard") { DashboardScreen(navController) }
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
