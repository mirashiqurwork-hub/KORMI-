package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var cardNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            val existing = db.workerDao().getWorkerByCard("503056")
            if (existing == null) {
                db.workerDao().insertWorker(
                    Worker(
                        workerId = "503056", photo = null, fullName = "NASIR HASSAIN",
                        phone = "01875829726", nidBirthCert = "1234567890", dateOfBirth = null,
                        presentAddress = "Sitakund, Bangladesh", section = "Ring", shift = "B",
                        weeklyOffDay = "SATURDAY", status = "Active", unit = "02", passwordHash = "1234"
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Kormi", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text(text = "Workforce Companion", fontSize = 14.sp, color = Gold, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            label = { Text("Card Number (or ADMIN)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (cardNumber.isBlank() || password.isBlank()) {
                    errorMessage = "কার্ড নম্বর এবং পাসওয়ার্ড দিন"
                } else {
                    isLoading = true
                    scope.launch {
                        // অ্যাডমিন লগইন চেক
                        if (cardNumber.equals("ADMIN", ignoreCase = true) && password == "ADMIN123") {
                            navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                        } else {
                            // ওয়ার্কার লগইন চেক
                            val worker = db.workerDao().getWorkerByCard(cardNumber)
                            if (worker != null && worker.passwordHash == password) {
                                navController.navigate("profile/${worker.workerId}") { popUpTo("login") { inclusive = true } }
                            } else {
                                errorMessage = "ভুল কার্ড নম্বর বা পাসওয়ার্ড!"
                                isLoading = false
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
