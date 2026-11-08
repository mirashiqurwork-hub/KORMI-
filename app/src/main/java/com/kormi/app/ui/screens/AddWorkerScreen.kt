package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
    var unit by remember { mutableStateOf("UNIT-01") }
    var section by remember { mutableStateOf("RING") }
    var shift by remember { mutableStateOf("A") }
    var offDay by remember { mutableStateOf("FRIDAY") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("1234") }
    
    // নতুন ফিল্ড
    var selectedRole by remember { mutableStateOf("Worker") }
    var selectedDesignation by remember { mutableStateOf("Helper") }

    var expandedRole by remember { mutableStateOf(false) }
    var expandedDesignation by remember { mutableStateOf(false) }

    val roles = listOf("Worker", "Officer", "Management", "Admin")
    
    // রোল অনুযায়ী ডেজিগনেশন
    val designations = when(selectedRole) {
        "Worker" -> listOf("L/R", "HELPER", "SR.HELPER", "JR.A.OP", "SR.A.OP", "OP", "LINE-MAN")
        "Officer" -> listOf("Supervisor", "APO", "SR APO", "PO", "SPO")
        "Management" -> listOf("APM", "DPM", "PM", "AGM", "DGM", "GM")
        else -> listOf("Admin", "Super Admin")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Person", color = Color.White, fontWeight = FontWeight.Bold) },
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
            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // রোল সিলেকশন
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedRole, onValueChange = {}, readOnly = true, label = { Text("Role *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                )
                Box(modifier = Modifier.matchParentSize().clickable { expandedRole = true })
                DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                    roles.forEach { role -> 
                        DropdownMenuItem(text = { Text(role) }, onClick = { 
                            selectedRole = role
                            selectedDesignation = when(role) {
                                "Worker" -> "Helper"
                                "Officer" -> "Supervisor"
                                "Management" -> "APM"
                                else -> "Admin"
                            }
                            expandedRole = false 
                        }) 
                    }
                }
            }

            // ডেজিগনেশন সিলেকশন
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedDesignation, onValueChange = {}, readOnly = true, label = { Text("Designation *") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                )
                Box(modifier = Modifier.matchParentSize().clickable { expandedDesignation = true })
                DropdownMenu(expanded = expandedDesignation, onDismissRequest = { expandedDesignation = false }) {
                    designations.forEach { desig -> 
                        DropdownMenuItem(text = { Text(desig) }, onClick = { selectedDesignation = desig; expandedDesignation = false }) 
                    }
                }
            }

            WorkerTextField("Full Name *", fullName) { fullName = it }
            WorkerTextField("Card Number / Login ID *", cardNumber) { cardNumber = it }
            WorkerTextField("Phone Number", phone) { phone = it }
            WorkerTextField("Unit (e.g., UNIT-01)", unit) { unit = it }
            
            // সেকশন ও শিফট (ম্যানেজমেন্ট এবং অ্যাডমিনের ক্ষেত্রে লাগবে না, তবে সহজ রাখার জন্য রাখা হলো)
            WorkerTextField("Section (e.g., RING)", section) { section = it }
            WorkerTextField("Shift (e.g., A)", shift) { shift = it }
            WorkerTextField("Weekly Off Day (e.g., FRIDAY)", offDay) { offDay = it }
            WorkerTextField("Address", address) { address = it }
            WorkerTextField("Password *", password) { password = it }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (fullName.isNotBlank() && cardNumber.isNotBlank() && password.isNotBlank()) {
                        scope.launch {
                            val newWorker = Worker(
                                workerId = cardNumber,
                                fullName = fullName,
                                role = selectedRole, // ড্রপডাউন থেকে আসছে
                                designation = selectedDesignation, // ড্রপডাউন থেকে আসছে
                                companyId = 1,
                                photo = null,
                                phone = phone,
                                nidBirthCert = "",
                                dateOfBirth = "",
                                presentAddress = address,
                                section = section,
                                shift = shift,
                                weeklyOffDay = offDay,
                                status = "Active",
                                unit = unit,
                                passwordHash = password,
                                salary = 500.0
                            )
                            db.workerDao().insertWorker(newWorker)
                            navController.popBackStack()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "সব তথ্য দিন", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Person", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WorkerTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
    )
}
