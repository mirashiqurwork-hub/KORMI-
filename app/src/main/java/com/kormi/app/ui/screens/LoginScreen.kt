package com.kormi.app.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, onLoginSuccess: (String, String) -> Unit) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7))) {
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h - 100f)
                lineTo(w * 0.1f, h - 150f)
                lineTo(w * 0.2f, h - 150f)
                lineTo(w * 0.2f, h - 250f)
                lineTo(w * 0.25f, h - 250f)
                lineTo(w * 0.25f, h - 180f)
                lineTo(w * 0.4f, h - 180f)
                lineTo(w * 0.45f, h - 220f)
                lineTo(w * 0.5f, h - 180f)
                lineTo(w * 0.6f, h - 180f)
                lineTo(w * 0.6f, h - 300f)
                lineTo(w * 0.65f, h - 300f)
                lineTo(w * 0.65f, h - 160f)
                lineTo(w * 0.8f, h - 160f)
                lineTo(w * 0.85f, h - 200f)
                lineTo(w * 1f, h - 120f)
                lineTo(w * 1f, h)
                close()
            }
            drawPath(path, Color(0xFFE8EDF2))

            drawCircle(Color(0xFFEDF1F5), radius = 40f, center = Offset(w * 0.22f, h - 300f))
            drawCircle(Color(0xFFEDF1F5), radius = 60f, center = Offset(w * 0.25f, h - 350f))
            drawCircle(Color(0xFFEDF1F5), radius = 80f, center = Offset(w * 0.3f, h - 420f))
            drawCircle(Color(0xFFEDF1F5), radius = 50f, center = Offset(w * 0.62f, h - 350f))
            drawCircle(Color(0xFFEDF1F5), radius = 70f, center = Offset(w * 0.66f, h - 400f))
            drawCircle(Color(0xFFEDF1F5), radius = 90f, center = Offset(w * 0.7f, h - 480f))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Kormi", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(text = "Workforce Companion", fontSize = 14.sp, color = Gold, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("Card Number / Mobile / Email") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Navy) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.6f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                    focusedBorderColor = Navy,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.8f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Navy) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password Visibility",
                            tint = Navy
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.6f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.6f),
                    focusedBorderColor = Navy,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.8f)
                )
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val inputId = userId.trim()
                    val inputPass = password.trim()
                    
                    if (inputId.isBlank() || inputPass.isBlank()) {
                        errorMessage = "সব তথ্য পূরণ করুন"
                    } else {
                        isLoading = true
                        errorMessage = ""
                        scope.launch {
                            if (inputId.equals("ADMIN", ignoreCase = true) && inputPass == "ADMIN123") {
                                val sharedPref = context.getSharedPreferences("KormiPrefs", Context.MODE_PRIVATE)
                                sharedPref.edit().putString("userId", "ADMIN").putString("role", "ADMIN").apply()

                                onLoginSuccess("ADMIN", "ADMIN")
                                navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                            } else {
                                val worker = db.workerDao().getWorkerForLogin(inputId)
                                if (worker != null && worker.passwordHash == inputPass) {
                                    val sharedPref = context.getSharedPreferences("KormiPrefs", Context.MODE_PRIVATE)
                                    sharedPref.edit().putString("userId", worker.workerId).putString("role", worker.role).apply()

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
                colors = ButtonDefaults.buttonColors(containerColor = Navy.copy(alpha = 0.9f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
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

            TextButton(onClick = { navController.navigate("register") }) {
                Text("Create New Account", color = Navy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
