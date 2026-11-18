package com.kormi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kormi.app.data.local.KormiDatabase
import com.kormi.app.data.local.entity.Worker
import com.kormi.app.data.prefs.SessionManager
import com.kormi.app.ui.screens.DashboardScreen
import com.kormi.app.ui.screens.LoginScreen
import com.kormi.app.ui.theme.KormiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KormiTheme {
                KormiApp()
            }
        }
    }
}

@Composable
fun KormiApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val session = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var loggedInWorker by remember { mutableStateOf<Worker?>(null) }
    var currentUserRole by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // App চালু হলে Session থেকে Worker লোড করো
    LaunchedEffect(Unit) {
        val savedId = session.getWorkerId()
        val savedRole = session.getRole()
        if (!savedId.isNullOrBlank()) {
            currentUserRole = savedRole ?: ""
            try {
                val db = KormiDatabase.getInstance(context)
                loggedInWorker = db.workerDao().getWorkerById(savedId)
            } catch (e: Exception) {
                loggedInWorker = null
            }
        }
        isLoading = false
    }

    if (isLoading) {
        // Loading - কিছু দেখানোর দরকার নাই, KormiTheme এর ভিতরে থাকবে
        return
    }

    val startDestination = if (session.isLoggedIn()) "dashboard" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = { userId, role ->
                    currentUserRole = role
                    scope.launch {
                        try {
                            val db = KormiDatabase.getInstance(context)
                            loggedInWorker = db.workerDao().getWorkerById(userId)
                        } catch (e: Exception) { }
                    }
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                navController = navController,
                loggedInWorker = loggedInWorker,
                currentUserRole = currentUserRole,
                onLogout = {
                    session.clearSession()
                    loggedInWorker = null
                    currentUserRole = ""
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        // বাকি Screen গুলা এখানে একই ভাবে পাঠাইতে হবে:
        // composable("access_control") {
        //     AccessControlScreen(
        //         loggedInWorker = loggedInWorker,
        //         currentUserRole = currentUserRole
        //     )
        // }
    }
}
