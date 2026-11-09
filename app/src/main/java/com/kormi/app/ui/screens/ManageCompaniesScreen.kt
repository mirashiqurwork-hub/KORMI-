package com.kormi.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCompaniesScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val companies by db.companyDao().getAllCompanies().collectAsState(initial = emptyList())
    
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCompany by remember { mutableStateOf<Company?>(null) }
    var nightBill by remember { mutableStateOf("") }
    var shiftChangeDay by remember { mutableStateOf("FRIDAY") }

    val days = listOf("SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Companies", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(companies) { company ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(company.companyName, fontWeight = FontWeight.Bold, color = Navy, fontSize = 18.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Night Bill: ${company.nightBillAmount} Tk", fontSize = 14.sp, color = TextSecondary)
                        Text("Shift Change Day: ${company.shiftChangeDay}", fontSize = 14.sp, color = TextSecondary)
                        
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                selectedCompany = company
                                nightBill = company.nightBillAmount.toString()
                                shiftChangeDay = company.shiftChangeDay
                                showEditDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Navy)
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    db.companyDao().deleteCompanyById(company.companyId)
                                    Toast.makeText(context, "কোম্পানি ডিলিট হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        // এডিট ডায়ালগ
        if (showEditDialog && selectedCompany != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit ${selectedCompany!!.companyName}", color = Navy, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = nightBill,
                            onValueChange = { nightBill = it },
                            label = { Text("Night Bill Amount (Tk)") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        // Shift Change Day Dropdown (সিম্পল)
                        var expandedDay by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = shiftChangeDay, onValueChange = {}, readOnly = true, label = { Text("Shift Change Day") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth()
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { expandedDay = true })
                            DropdownMenu(expanded = expandedDay, onDismissRequest = { expandedDay = false }) {
                                days.forEach { day ->
                                    DropdownMenuItem(text = { Text(day) }, onClick = { shiftChangeDay = day; expandedDay = false })
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val updated = selectedCompany!!.copy(
                                    nightBillAmount = nightBill.toDoubleOrNull() ?: 20.0,
                                    shiftChangeDay = shiftChangeDay
                                )
                                db.companyDao().insertCompany(updated)
                                showEditDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) {
                        Text("Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
