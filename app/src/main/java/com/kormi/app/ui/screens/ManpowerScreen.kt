package com.kormi.app.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
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
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManpowerScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val calendar = Calendar.getInstance()
    var selectedDateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)) }
    
    var totalPresent by remember { mutableStateOf(0) }
    var totalAbsent by remember { mutableStateOf(0) }
    var totalOffDay by remember { mutableStateOf(0) }
    var sectionSummary by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedDateStr) {
        isLoading = true
        scope.launch {
            val workers = db.workerDao().getAllActiveWorkersList()
            val attendanceList = db.attendanceDao().getAttendanceByDate(selectedDateStr)
            val attendanceMap = attendanceList.associate { it.workerId to it.status }

            var present = 0
            var absent = 0
            var offDay = 0
            val secMap = mutableMapOf<String, Int>()

            workers.forEach { worker ->
                val status = attendanceMap[worker.workerId] ?: "Unmarked" 
                when (status) {
                    "Present" -> {
                        present++
                        val sec = worker.section ?: "Unknown"
                        secMap[sec] = (secMap[sec] ?: 0) + 1
                    }
                    "Absent" -> absent++
                    "Off Day" -> offDay++
                }
            }
            totalPresent = present
            totalAbsent = absent
            totalOffDay = offDay
            sectionSummary = secMap.toList().sortedBy { it.first }.toMap()
            isLoading = false
        }
    }

    // টেক্সট সামারি তৈরি করার ফাংশন
    fun generateSummaryText(): String {
        val dateDisplay = selectedDateStr.replace("-", ".")
        var text = "Manpower Summary\n"
        text += "Date: $dateDisplay\n\n"
        
        sectionSummary.forEach { (section, count) ->
            text += "$section: $count\n"
        }
        
        text += "\nTotal Present: $totalPresent\n"
        text += "Total Absent: $totalAbsent\n"
        text += "Total Off Day: $totalOffDay\n"
        
        return text
    }

    val datePickerDialog = DatePickerDialog(
        context, { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manpower Summary", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // শেয়ার বাটন
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, generateSummaryText())
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    // ডেট পিকার বাটন
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Date", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF2F4F7)).padding(16.dp)) {
            Text(text = "Report Date: $selectedDateStr", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy, modifier = Modifier.padding(bottom = 16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Navy) }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard("Present", totalPresent.toString(), Color(0xFF4CAF50), Modifier.weight(1f))
                    SummaryCard("Absent", totalAbsent.toString(), Color(0xFFF44336), Modifier.weight(1f))
                    SummaryCard("Off Day", totalOffDay.toString(), Color(0xFFFFC107), Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
                Text("Section-wise Present", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(12.dp))

                if (sectionSummary.isEmpty()) {
                    Text("এই তারিখে কোনো এটেনডেন্স ডাটা পাওয়া যায়নি।", color = TextSecondary)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sectionSummary.toList()) { (section, count) ->
                            SectionRow(section, count)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun SectionRow(section: String, count: Int) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = section, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Navy)
            Text(text = "$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gold)
        }
    }
}
