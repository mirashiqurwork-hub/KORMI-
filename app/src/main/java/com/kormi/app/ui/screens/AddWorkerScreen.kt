package com.kormi.app.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Company
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Navy
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var cardNumber by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var nid by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    
    var selectedRole by remember { mutableStateOf("Worker") }
    var selectedDesignation by remember { mutableStateOf("L/R") }
    var selectedCompany by remember { mutableStateOf<Company?>(null) }
    var selectedShift by remember { mutableStateOf("A") }
    var selectedSection by remember { mutableStateOf("BACKSIDE") }
    var selectedUnit by remember { mutableStateOf("UNIT-01") }
    var selectedOffDay by remember { mutableStateOf("FRIDAY") }
    var selectedStatus by remember { mutableStateOf("Active") }

    var companies by remember { mutableStateOf<List<Company>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            val existingCompanies = db.companyDao().getAllCompanies()
            existingCompanies.collect { list ->
                if (list.isEmpty()) {
                    db.companyDao().insertCompany(Company(companyName = "Kormi Spinning Mills", nightBillAmount = 20.0, shiftChangeDay = "FRIDAY"))
                } else {
                    companies = list
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) photoUri = uri
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> dob = "$dayOfMonth/${month + 1}/$year" },
        Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Account", color = Color.White) },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(Navy.copy(alpha = 0.1f)).clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(model = photoUri, contentDescription = "Photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Person, contentDescription = "Photo", tint = Navy, modifier = Modifier.size(50.dp))
                }
            }
            Text("Upload Photo", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))

            RegTextField("Card Number / Email *", cardNumber) { cardNumber = it }
            RegTextField("Full Name *", fullName) { fullName = it }
            RegTextField("NID / Birth Cert No", nid) { nid = it }
            
            OutlinedTextField(
                value = dob, onValueChange = { }, label = { Text("Date of Birth") }, readOnly = true,
                trailingIcon = { IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Default.DateRange, null) } },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            RegTextField("Salary (Tk)", salary) { salary = it }
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password *") },
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            DropdownField("Company *", selectedCompany?.companyName ?: "Select Company", companies.map { it.companyName }) { index ->
                selectedCompany = companies[index]
            }

            DropdownField("Role *", selectedRole, listOf("Worker", "Officer", "Management", "Admin")) { index ->
                selectedRole = listOf("Worker", "Officer", "Management", "Admin")[index]
                selectedDesignation = when(selectedRole) {
                    "Worker" -> "L/R"
                    "Officer" -> "Supervisor"
                    "Management" -> "APM"
                    else -> "Admin"
                }
            }

            when (selectedRole) {
                "Worker" -> {
                    val workerTypes = listOf("L/R", "HELPER", "SR.HELPER", "JR.A.OP", "SR.A.OP", "OP", "LINE-MAN")
                    DropdownField("Worker Type", selectedDesignation, workerTypes) { selectedDesignation = workerTypes[it] }
                    DropdownField("Shift", selectedShift, listOf("P/A-R/A", "P/B-R/B", "P/C-R/C")) { selectedShift = listOf("P/A-R/A", "P/B-R/B", "P/C-R/C")[it] }
                    DropdownField("Section", selectedSection, listOf("BACKSIDE", "RING", "AUTOCONE", "PACKING")) { selectedSection = listOf("BACKSIDE", "RING", "AUTOCONE", "PACKING")[it] }
                }
                "Officer" -> {
                    val officerTypes = listOf("Supervisor", "APO", "SR APO", "PO", "SPO")
                    DropdownField("Officer Type", selectedDesignation, officerTypes) { selectedDesignation = officerTypes[it] }
                    DropdownField("Shift", selectedShift, listOf("P/A-R/A", "P/B-R/B", "P/C-R/C")) { selectedShift = listOf("P/A-R/A", "P/B-R/B", "P/C-R/C")[it] }
                    DropdownField("Section", selectedSection, listOf("ALL SECTION", "BACKSIDE", "RING", "AUTOCONE", "PACKING")) { selectedSection = listOf("ALL SECTION", "BACKSIDE", "RING", "AUTOCONE", "PACKING")[it] }
                }
                "Management" -> {
                    val mgmtTypes = listOf("APM", "DPM", "PM", "AGM", "DGM", "GM")
                    DropdownField("Management Type", selectedDesignation, mgmtTypes) { selectedDesignation = mgmtTypes[it] }
                }
                "Admin" -> {
                    val adminTypes = listOf("Admin", "Super Admin")
                    DropdownField("Admin Type", selectedDesignation, adminTypes) { selectedDesignation = adminTypes[it] }
                }
            }

            if (selectedRole == "Management" || selectedRole == "Admin") {
                DropdownField("Unit", selectedUnit, listOf("ALL UNIT", "UNIT-01", "UNIT-02", "G/F", "F/F")) { selectedUnit = listOf("ALL UNIT", "UNIT-01", "UNIT-02", "G/F", "F/F")[it] }
            } else {
                DropdownField("Unit", selectedUnit, listOf("UNIT-01", "UNIT-02", "G/F", "F/F")) { selectedUnit = listOf("UNIT-01", "UNIT-02", "G/F", "F/F")[it] }
            }

            if (selectedRole == "Worker" || selectedRole == "Officer") {
                DropdownField("Weekly Off Day", selectedOffDay, listOf("FRIDAY", "SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY")) { selectedOffDay = listOf("FRIDAY", "SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY")[it] }
            }

            DropdownField("Status", selectedStatus, listOf("Active", "Inactive", "Pending")) { selectedStatus = listOf("Active", "Inactive", "Pending")[it] }

            OutlinedTextField(
                value = address, onValueChange = { address = it }, label = { Text("Present Address") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (cardNumber.isBlank() || fullName.isBlank() || password.isBlank() || selectedCompany == null) {
                        Toast.makeText(context, "অনুগ্রহ করে কোম্পানি এবং প্রয়োজনীয় তথ্য পূরণ করুন!", Toast.LENGTH_LONG).show()
                    } else {
                        scope.launch {
                            val newWorker = Worker(
                                workerId = cardNumber.trim(),
                                fullName = fullName.trim(),
                                role = selectedRole,
                                designation = selectedDesignation,
                                companyId = selectedCompany!!.companyId,
                                photo = photoUri?.toString(),
                                nidBirthCert = nid,
                                dateOfBirth = dob,
                                presentAddress = address,
                                shift = selectedShift,
                                section = selectedSection,
                                unit = selectedUnit,
                                weeklyOffDay = selectedOffDay,
                                status = selectedStatus,
                                salary = salary.toDoubleOrNull() ?: 0.0,
                                passwordHash = password.trim()
                            )
                            db.workerDao().insertWorker(newWorker)
                            Toast.makeText(context, "সফলভাবে একাউন্ট তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
