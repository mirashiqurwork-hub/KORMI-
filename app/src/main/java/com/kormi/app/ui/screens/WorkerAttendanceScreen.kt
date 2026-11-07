package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerAttendanceScreen(worker: Worker) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    var attendanceMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthStr = String.format(Locale.US, "%04d-%02d", year, month + 1)

    LaunchedEffect(Unit) {
        val myAttendance = db.attendanceDao().getWorkerAttendance(worker.workerId)
        val map = mutableMapOf<Int, String>()
        myAttendance.forEach { att ->
            if (att.date.startsWith(monthStr)) {
                try { map[att.date.split("-").last().toInt()] = att.status } catch (e: Exception) {}
            }
        }
        attendanceMap = map
    }

    var presentCount = 0
    var absentCount = 0
    var offDayCount = 0
    attendanceMap.forEach { (_, status) ->
        when(status) {
            "Present" -> presentCount++
            "Absent" -> absentCount++
            "Off Day" -> offDayCount++
        }
    }

    // পরিবর্তন এখানে (dailyWage এর বদলে salary)
    val dailyWage = if (worker.salary > 0) worker.salary else 500.0
    val estimatedSalary = presentCount * dailyWage

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Attendance", color = Color.White, fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Navy), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Estimated Salary (${SimpleDateFormat("MMMM", Locale.US).format(calendar.time)})", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text("৳ ${String.format(Locale.US, "%.2f", estimatedSalary)}", color = Gold, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        Text("Present: $presentCount", color = Color.White, fontSize = 12.sp)
                        Text("Absent: $absentCount", color = Color.White, fontSize = 12.sp)
                        Text("Off Day: $offDayCount", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Text("Attendance Calendar", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))
            
            LazyVerticalGrid(columns = GridCells.Fixed(7), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(count = maxDays) { index ->
                    val day = index + 1
                    val status = attendanceMap[day] ?: "Unmarked"
                    val bgColor = when(status) {
                        "Present" -> Color(0xFF4CAF50)
                        "Absent" -> Color(0xFFF44336)
                        "Off Day" -> Color(0xFFFFC107)
                        else -> Color.LightGray.copy(alpha = 0.5f)
                    }
                    val textColor = if (status == "Unmarked") Color.DarkGray else Color.White
                    Box(modifier = Modifier.aspectRatio(1f).clip(CircleShape).background(bgColor), contentAlignment = Alignment.Center) {
                        Text(day.toString(), color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("* এই ডাটা শুধুমাত্র দেখার জন্য।", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}
