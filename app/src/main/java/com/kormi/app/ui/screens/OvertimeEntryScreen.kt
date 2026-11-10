package com.kormi.app.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
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
import com.kormi.app.data.Overtime
import com.kormi.app.data.SyncHelper
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OvertimeEntryScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)) }
    
    var workerIdInput by remember { mutableStateOf("") }
    var overtimeHours by remember { mutableStateOf("") }
    var foundWorker by remember { mutableStateOf<Worker?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    val datePickerDialog = DatePickerDialog(
        context, { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    fun searchWorker() {
        if (workerIdInput.isBlank()) {
            Toast.makeText(context, "কার্ড নম্বর দিন", Toast.LENGTH_SHORT).show()
            return
        }
        isSearching = true
        scope.launch {
            foundWorker = db.workerDao().getWorkerByCard(workerIdInput.trim())
            if (foundWorker == null) {
                Toast.makeText(context, "এই কার্ড নম্বরে কোনো কর্মী পাওয়া যায়নি!", Toast.LENGTH_SHORT).show()
            }
            isSearching = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overtime Entry", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Date", tint = Color.White)
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
            Text("Date: $selectedDate", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = workerIdInput,
                            onValueChange = { workerIdInput = it },
                            label = { Text("Worker Card Number") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { searchWorker() },
                            modifier = Modifier.height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSearching
                        ) {
                            if (isSearching) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    }

                    if (foundWorker != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Navy.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Name: ${foundWorker!!.fullName}", fontWeight = FontWeight.Bold, color = Navy)
                                Text("Section: ${foundWorker!!.section ?: "N/A"} | Shift: ${foundWorker!!.shift ?: "N/A"}", fontSize = 12.sp, color = TextSecondary)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = overtimeHours,
                            onValueChange = { overtimeHours = it },
                            label = { Text("Overtime Hours (e.g., 2.5)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val hours = overtimeHours.toDoubleOrNull() ?: 0.0
                                if (hours > 0) {
                                    scope.launch {
                                        // লোকাল ডাটাবেসে সেভ
                                        db.overtimeDao().insertOvertime(
                                            Overtime(
                                                workerId = foundWorker!!.workerId,
                                                date = selectedDate,
                                                hours = hours
                                            )
                                        )
                                        
                                        // গুগল শিটে সিঙ্ক
                                        val syncArray = JSONArray()
                                        val jsonObj = JSONObject()
                                        jsonObj.put("date", selectedDate)
                                        jsonObj.put("workerId", foundWorker!!.workerId)
                                        jsonObj.put("hours", hours)
                                        syncArray.put(jsonObj)
                                        SyncHelper.postData("syncOvertime", syncArray)

                                        Toast.makeText(context, "ওভারটাইম সেভ ও সিঙ্ক হয়েছে!", Toast.LENGTH_SHORT).show()
                                        workerIdInput = ""
                                        overtimeHours = ""
                                        foundWorker = null
                                    }
                                } else {
                                    Toast.makeText(context, "সঠিক ঘণ্টা লিখুন!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Overtime", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                        }
                    }
                }
            }
        }
    }
}
