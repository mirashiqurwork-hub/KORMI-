package com.kormi.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, onLoginSuccess: (String, String) -> Unit) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        
        // Background Factory Silhouette (কারখানার হালকা শেডো)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val paint = androidx.compose.ui.graphics.Paint().apply { color = Color(0xFFF0F4F8) }
            
            // Factory Shapes (Hut, Chimney)
            val path = Path().apply {
                moveTo(0f, height * 0.85f)
                lineTo(width * 0.1f, height * 0.85f)
                lineTo(width * 0.1f, height * 0.80f)
                lineTo(width * 0.15f, height * 0.80f)
                lineTo(width * 0.15f, height * 0.85f)
                lineTo(width * 0.25f, height * 0.85f)
                lineTo(width * 0.25f, height * 0.75f)
                lineTo(width * 0.30f, height * 0.75f)
                lineTo(width * 0.30f, height * 0.85f)
                lineTo(width * 0.55f, height * 0.85f)
                lineTo(width * 0.55f, height * 0.82f)
                lineTo(width * 0.60f, height * 0.82f)
                lineTo(width * 0.60f, height * 0.87f)
                lineTo(width * 0.80f, height * 0.87f)
                lineTo(width * 0.80f, height * 0.78f)
                lineTo(width * 0.85f, height * 0.78f)
                lineTo(width * 0.85f, height * 0.87f)
                lineTo(width, height * 0.87f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(path, Color(0xFFF7F9FC))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Title
            Text(text = "Kormi", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(text = "Workforce Management", fontSize = 16.sp, color = Gold, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)

            Spacer(modifier = Modifier.height(48.dp))

            // User ID Field
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("Card Number / Mobile / Email") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Navy) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Navy,
                    focusedLabelColor = Navy,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Navy) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Navy,
                    focusedLabelColor = Navy,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = {
                    val inputId = userId.trim()
                    val inputPass = password.trim() // স্পেস ইগনোর করা হচ্ছে
                    
                    if (inputId.isBlank() || inputPass.isBlank()) {
                        errorMessage = "সব তথ্য পূরণ করুন"
                    } else {
                        isLoading = true
                        errorMessage = ""
                        scope.launch {
                            // অ্যাডমিন লগইন চেক
                            if (inputId.equals("ADMIN", ignoreCase = true) && inputPass == "ADMIN123") {
                                onLoginSuccess("ADMIN", "ADMIN")
                                navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                            } else {
                                val worker = db.workerDao().getWorkerForLogin(inputId)
                                if (worker != null && worker.passwordHash == inputPass) {
                                    onLoginSuccess(worker.workerId, worker.role)
                                    if (worker.role == "Worker") {
                                        navController.navigate("worker_dashboard") { popUpTo("login") { inclusive = true } }
                                    } else {
                                        navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                                    }
                                } else {
                                    errorMessage = "ভুল তথ্য বা পাসওয়ার্ড!"
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Create Account & Forgot Password Links
            TextButton(onClick = { navController.navigate("register") }) {
                Text("Create New Account", color = Navy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = { /* Forgot Password Logic */ }) {
                Text("Forgot Password?", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}
