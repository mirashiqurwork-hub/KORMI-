package com.kormi.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F4F7))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Date: $selectedDate", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White), 
                shape = RoundedCornerShape(12.dp), 
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    
                    // Shift (সাধারণ টেক্সট বক্স)
                    OutlinedTextField(
                        value = shift, 
                        onValueChange = { shift = it }, 
                        label = { Text("Shift (A, B, C)") }, 
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Section (সাধারণ টেক্সট বক্স)
                    OutlinedTextField(
                        value = section, 
                        onValueChange = { section = it }, 
                        label = { Text("Section (BACKSIDE, RING)") }, 
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = machineName, 
                        onValueChange = { machineName = it }, 
                        label = { Text("Machine Name/No") }, 
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = minutes, 
                        onValueChange = { minutes = it }, 
                        label = { Text("Breakdown Time (Minutes)") }, 
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = reason, 
                        onValueChange = { reason = it }, 
                        label = { Text("Reason") }, 
                        modifier = Modifier.fillMaxWidth()
                    )
                    
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
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) { 
                        Text("Save Breakdown") 
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Today's Breakdowns", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            // লিস্ট ভিউ (কোনো জটিল লেআউট নেই)
            breakdowns.forEach { bd ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White), 
                    shape = RoundedCornerShape(12.dp), 
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${bd.machineName} (${bd.section})", fontWeight = FontWeight.Bold, color = Navy)
                            Text("Shift: ${bd.shift} | Reason: ${bd.reason}", fontSize = 12.sp, color = TextSecondary)
                        }
                        Text("${bd.minutes} min", fontWeight = FontWeight.Bold, color = Gold, fontSize = 16.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
