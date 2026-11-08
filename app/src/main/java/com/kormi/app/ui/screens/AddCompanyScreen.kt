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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Company
import com.kormi.app.ui.theme.Navy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCompanyScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var companyName by remember { mutableStateOf("") }
    var nightBill by remember { mutableStateOf("20.0") }
    var shiftChangeDay by remember { mutableStateOf("FRIDAY") }
    
    var expandedDay by remember { mutableStateOf(false) }
    val days = listOf("SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Company", color = Color.White, fontWeight = FontWeight.Bold) },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = companyName, onValueChange = { companyName = it },
                label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )
            
            OutlinedTextField(
                value = nightBill, onValueChange = { nightBill = it },
                label = { Text("Night Bill Amount (Tk)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )

            // Shift Change Day Dropdown
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                OutlinedTextField(
                    value = shiftChangeDay, onValueChange = {}, label = { Text("Shift Change Day") }, readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                )
                Box(modifier = Modifier.matchParentSize().clickable { expandedDay = true })
                DropdownMenu(expanded = expandedDay, onDismissRequest = { expandedDay = false }) {
                    days.forEach { day ->
                        DropdownMenuItem(text = { Text(day) }, onClick = { shiftChangeDay = day; expandedDay = false })
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (companyName.isNotBlank()) {
                        scope.launch {
                            val newCompany = Company(
                                companyName = companyName.trim(),
                                nightBillAmount = nightBill.toDoubleOrNull() ?: 20.0,
                                shiftChangeDay = shiftChangeDay
                            )
                            db.companyDao().insertCompany(newCompany)
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Company", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
