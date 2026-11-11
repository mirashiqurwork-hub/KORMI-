package com.kormi.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(mainNavController: NavController, worker: Worker?, currentUserRole: String = "Worker") {
    if (worker == null) return
    
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    
    var expanded by remember { mutableStateOf(false) }
    var currentPhotoUri by remember { mutableStateOf(worker.photo) }
    var showEditAdminDialog by remember { mutableStateOf(false) }
    var adminName by remember { mutableStateOf(worker.fullName) }

    // স্যালারি ও এটেনডেন্স স্টেট
    var presentCount by remember { mutableStateOf(0) }
    var absentCount by remember { mutableStateOf(0) }
    var offDayCount by remember { mutableStateOf(0) }
    var totalOvertimeHours by remember { mutableStateOf(0.0) }
    var nightBillRate by remember { mutableStateOf(20.0) }
    
    var baseSalary by remember { mutableStateOf(0.0) }
    var overtimeAmount by remember { mutableStateOf(0.0) }
    var nightBillAmount by remember { mutableStateOf(0.0) }
    var totalSalary by remember { mutableStateOf(0.0) }

    LaunchedEffect(worker.workerId) {
        if (currentUserRole != "Worker") {
            val attendanceList = db.attendanceDao().getWorkerAttendance(worker.workerId)
            val currentMonthStr = android.text.format.DateFormat.format("yyyy-MM", System.currentTimeMillis()).toString()
            
            var present = 0
            var absent = 0
            var offDay = 0
            var nightShift = 0

            attendanceList.forEach { att ->
                if (att.date.startsWith(currentMonthStr)) {
                    when(att.status) {
                        "Present" -> {
                            present++
                            if (worker.shift.equals("P/C", ignoreCase = true)) nightShift++
                        }
                        "Absent" -> absent++
                        "Off Day" -> offDay++
                    }
                }
            }
            presentCount = present
            absentCount = absent
            offDayCount = offDay

            val otList = db.overtimeDao().getOvertimeByWorker(worker.workerId)
            totalOvertimeHours = otList.sumOf { it.hours }

            val company = db.companyDao().getCompanyById(worker.companyId)
            if (company != null) {
                nightBillRate = company.nightBillAmount
            }

            val maxDays = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            val dailyWage = if (worker.salary > 0 && maxDays > 0) (worker.salary / maxDays) else 0.0
            
            baseSalary = present * dailyWage
            overtimeAmount = totalOvertimeHours * 50.0
            nightBillAmount = nightShift * nightBillRate
            totalSalary = baseSalary + overtimeAmount + nightBillAmount
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            currentPhotoUri = uri.toString()
            scope.launch { db.workerDao().updatePhoto(worker.workerId, uri.toString()) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (currentUserRole != "Worker") {
                        IconButton(onClick = { mainNavController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Person, contentDescription = "Menu", tint = Color.White)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Profile Settings") }, leadingIcon = { Icon(Icons.Default.Settings, null) }, onClick = { expanded = false })
                            DropdownMenuItem(text = { Text("Logout") }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) }, onClick = { 
                                expanded = false
                                val sharedPref = context.getSharedPreferences("KormiPrefs", Context.MODE_PRIVATE)
                                sharedPref.edit().clear().apply()
                                mainNavController.navigate("login") { popUpTo(0) { inclusive = true } } 
                            })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F4F7))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🌟 গ্লাস কার্ড ডিজাইন
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.4f))))
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentPhotoUri != null) {
                        AsyncImage(model = currentPhotoUri, contentDescription = "Profile", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Default.Person, contentDescription = "Photo", tint = Navy, modifier = Modifier.size(60.dp))
                    }
                }
                Text("ছবি পরিবর্তন করতে ক্লিক করুন", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))

                Spacer(Modifier.height(16.dp))
                Text(text = worker.fullName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text(text = "Card: ${worker.workerId}", fontSize = 14.sp, color = Gold, fontWeight = FontWeight.Medium)
                
                if (currentUserRole == "Admin" || currentUserRole == "ADMIN" || currentUserRole == "Management" || currentUserRole == "Officer") {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { 
                            if (worker.workerId == "ADMIN") {
                                showEditAdminDialog = true
                            } else {
                                mainNavController.navigate("edit_worker/${worker.workerId}") 
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Information")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 🌟 গ্লাস ইনফরমেশন কার্ড
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileRow("Phone", worker.phone)
                        ProfileRow("Role", worker.role)
                        ProfileRow("Designation", worker.designation)
                        ProfileRow("Unit", worker.unit ?: "N/A")
                        ProfileRow("Section", worker.section ?: "N/A")
                        ProfileRow("Shift", worker.shift ?: "N/A")
                        ProfileRow("Weekly Off Day", worker.weeklyOffDay ?: "N/A")
                        ProfileRow("Status", worker.status)
                        ProfileRow("Monthly Salary", "${worker.salary} Tk")
                        ProfileRow("Address", worker.presentAddress ?: "N/A")
                    }
                }

                if (currentUserRole != "Worker") {
                    Spacer(Modifier.height(16.dp))
                    Text("Current Month Salary & Attendance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy, modifier = Modifier.align(Alignment.Start))
                    Spacer(Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(), 
                        shape = RoundedCornerShape(20.dp), 
                        colors = CardDefaults.cardColors(containerColor = Navy.copy(alpha = 0.95f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Estimated Salary", color = Color.White, fontSize = 16.sp)
                                Text("৳ ${String.format(Locale.US, "%.2f", totalSalary)}", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.2f))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Present", color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Absent", color = Color(0xFFF44336), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Off Day", color = Color(0xFFFFC107), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$presentCount Days", color = Color.White, fontSize = 14.sp)
                                Text("$absentCount Days", color = Color.White, fontSize = 14.sp)
                                Text("$offDayCount Days", color = Color.White, fontSize = 14.sp)
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.2f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Base Salary", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                Text("৳ ${String.format(Locale.US, "%.2f", baseSalary)}", color = Color.White, fontSize = 14.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Overtime ($totalOvertimeHours Hours)", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                Text("৳ ${String.format(Locale.US, "%.2f", overtimeAmount)}", color = Color.White, fontSize = 14.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Night Bill", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                Text("৳ ${String.format(Locale.US, "%.2f", nightBillAmount)}", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // 🌟 এডমিনের নাম এডিট করার ডায়ালগ
    if (showEditAdminDialog) {
        AlertDialog(
            onDismissRequest = { showEditAdminDialog = false },
            title = { Text("Edit Admin Name", fontWeight = FontWeight.Bold, color = Navy) },
            text = {
                OutlinedTextField(
                    value = adminName, 
                    onValueChange = { adminName = it }, 
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val updatedAdmin = worker.copy(fullName = adminName)
                            db.workerDao().updateWorker(updatedAdmin)
                            showEditAdminDialog = false
                            // রিফ্রেশ করার জন্য অ্যাক্টিভিটি রিক্রিয়েট বা ডাটা আপডেট নোটিফাই
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditAdminDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Text(text = value, color = Navy, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
