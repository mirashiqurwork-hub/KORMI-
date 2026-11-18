package com.kormi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kormi.app.ui.screens.DashboardScreen
import com.kormi.app.ui.screens.LoginScreen
import com.kormi.app.ui.theme.KormiTheme

// নিচের ১টা লাইন তোমার DashboardScreen.kt থেকে কপি করে এখানে বসাও
// DashboardScreen.kt খুললে উপরে লেখা থাকবে: import ... FontScaleOption
// ওই লাইনটা এখানে বসাও। যেমন: import com.kormi.app.data.prefs.FontScaleOption
import com.kormi.app.data.prefs.FontScaleOption

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
    val navController = rememberNavController()

    var role by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var currentFontScale by remember { mutableStateOf(FontScaleOption.NORMAL) }

    // Login করা থাকলে dashboard, না থাকলে login
    val startDestination = if (userId.isNotBlank()) "dashboard" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = { id, r ->
                    userId = id
                    role = r
                    userName = id
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                navController = navController,
                role = role,
                userName = userName,
                userId = userId,
                currentFontScale = currentFontScale,
                onFontScaleChange = { currentFontScale = it }
            )
        }
    }
}
