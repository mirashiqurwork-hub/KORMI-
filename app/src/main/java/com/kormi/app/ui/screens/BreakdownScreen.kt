package com.kormi.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import com.kormi.app.data.Breakdown
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreakdownScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)) }
    
    // ফর্ম স্টেট
    var shift by remember { mutableStateOf("A") }
    var section by remember { mutableStateOf("BACKSIDE") }
    var machineName by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    var expandedShift by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf(false) }

    val shifts = listOf("P/A", "P/B", "P/C")
    val sections = listOf("BACKSIDE", "RING", "AUTOCONE", "PACKING")

    // ডাটাবেস থেকে ওই তারিখের ব্রেকডাউন লিস্ট
    val breakdowns by db.breakdownDao().getBreakdownsByDate(selectedDate).collectAsState(initial = emptyList())

    val datePickerDialog = DatePickerDialog(
        context, { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Machine Breakdown", color = Color.White, fontWeight = FontWeight.Bold) },
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
        // LazyColumn ব্যবহার করে পুরো স্ক্রিন সাজানো হলো (ক্র্যাশ-ফ্রি এবং স্ক্রলযোগ্য)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F4F7))
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // ডেট পিকার হেডার
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Navy.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = Navy)
                            Spacer(Modifier.width(8.dp))
                            Text("Date: $selectedDate", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // এন্ট্রি ফর্ম
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White), 
                        shape = RoundedCornerShape(16.dp), 
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            
                            Text("Breakdown Entry", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Shift Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = expandedShift,
                                    onExpandedChange = { expandedShift = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = shift,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Shift") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedShift) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedShift,
                                        onDismissRequest = { expandedShift = false }
                                    ) {
                                        shifts.forEach { s ->
                                            DropdownMenuItem(
                                                text = { Text(s) },
                                                onClick = { shift = s; expandedShift = false }
                                            )
                                        }
                                    }
                                }

                                // Section Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = expandedSection,
                                    onExpandedChange = { expandedSection = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = section,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Section") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSection) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedSection,
                                        onDismissRequest = { expandedSection = false }
                                    ) {
                                        sections.forEach { s ->
                                            DropdownMenuItem(
                                                text = { Text(s) },
                                                onClick = { section = s; expandedSection = false }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            OutlinedTextField(
                                value = machineName, 
                                onValueChange = { machineName = it }, 
                                label = { Text("Machine Name/No") }, 
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            OutlinedTextField(
                                value = minutes, 
                                onValueChange = { minutes = it }, 
                                label = { Text("Breakdown Time (Minutes)") }, 
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            OutlinedTextField(
                                value = reason, 
                                onValueChange = { reason = it }, 
                                label = { Text("Reason") }, 
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    if (machineName.isNotBlank() && minutes.isNotBlank()) {
                                        scope.launch {
                                            db.breakdownDao().insertBreakdown(
                                                Breakdown(
                                                    date = selectedDate, 
                                                    shift = shift, 
                                                    section = section,
                                                    machineName = machineName, 
                                                    minutes = minutes.toIntOrNull() ?: 0,
                                                    reason = reason, 
                                                    reportedBy = "Admin"
                                                )
                                            )
                                            machineName = ""; minutes = ""; reason = ""
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp), 
                                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                                shape = RoundedCornerShape(12.dp)
                            ) { 
                                Text("Save Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp) 
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Today's Breakdown List", fontWeight = FontWeight.Bold, color = Navy, fontSize = 18.sp)
                }
            }

            // লিস্ট ভিউ
            items(breakdowns) { bd ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White), 
                    shape = RoundedCornerShape(12.dp), 
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${bd.machineName} (${bd.section})", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Shift: ${bd.shift} | Reason: ${bd.reason}", fontSize = 12.sp, color = TextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .background(Gold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${bd.minutes} min", fontWeight = FontWeight.Bold, color = Gold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // নিচে ফাঁকা জায়গা
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
