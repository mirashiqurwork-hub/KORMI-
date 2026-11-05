package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Navy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var shift by remember { mutableStateOf("") }
    var offDay by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Worker", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F4F7))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WorkerTextField("Full Name *", fullName) { fullName = it }
            WorkerTextField("Card Number *", cardNumber) { cardNumber = it }
            WorkerTextField("Phone Number", phone) { phone = it }
            WorkerTextField("Unit (e.g., 02)", unit) { unit = it }
            WorkerTextField("Section (e.g., Ring)", section) { section = it }
            WorkerTextField("Shift (e.g., B)", shift) { shift = it }
            WorkerTextField("Weekly Off Day (e.g., SATURDAY)", offDay) { offDay = it }
            WorkerTextField("Address", address) { address = it }
            WorkerTextField("Password *", password) { password = it }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (fullName.isNotBlank() && cardNumber.isNotBlank() && password.isNotBlank()) {
                        scope.launch {
                            val newWorker = Worker(
                                workerId = cardNumber,
                                photo = null,
                                fullName = fullName,
                                phone = phone,
                                nidBirthCert = "",
                                dateOfBirth = "",
                                presentAddress = address,
                                section = section,
                                shift = shift,
                                weeklyOffDay = offDay,
                                status = "Active",
                                unit = unit,
                                passwordHash = password
                            )
                            db.workerDao().insertWorker(newWorker)
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Worker", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WorkerTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}
