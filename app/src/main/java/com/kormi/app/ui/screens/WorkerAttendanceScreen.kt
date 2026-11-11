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
    
    var totalOvertimeHours by remember { mutableStateOf(0.0) }
    var nightBillRate by remember { mutableStateOf(20.0) }
    
    var baseSalary by remember { mutableStateOf(0.0) }
    var overtimeAmount by remember { mutableStateOf(0.0) }
    var nightBillAmount by remember { mutableStateOf(0.0) }
    var totalSalary by remember { mutableStateOf(0.0) }

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

        val otList = db.overtimeDao().getMonthlyOvertime(worker.workerId, monthStr)
        totalOvertimeHours = otList.sumOf { it.hours }

        val company = db.companyDao().getCompanyById(worker.companyId)
        if (company != null) {
            nightBillRate = company.nightBillAmount
        }
    }

    var presentCount = 0
    var absentCount = 0
    var offDayCount = 0
    var nightShiftCount = 0

    attendanceMap.forEach { (_, status) ->
        when(status) {
            "Present" -> {
                presentCount++
                if (worker.shift.equals("P/C", ignoreCase = true)) nightShiftCount++
            }
            "Absent" -> absentCount++
            "Off Day" -> offDayCount++
        }
    }

    val dailyWage = if (worker.salary > 0 && maxDays > 0) (worker.salary / maxDays) else 0.0
    baseSalary = presentCount * dailyWage
    overtimeAmount = totalOvertimeHours * 50.0
    nightBillAmount = nightShiftCount * nightBillRate
    totalSalary = baseSalary + overtimeAmount + nightBillAmount

    val totalWorkingDays = presentCount + absentCount
    val presentRatio = if (totalWorkingDays > 0) (presentCount.toFloat() / totalWorkingDays.toFloat() * 100).toInt() else 0

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Attendance", color = Color.White, fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding).padding(16.dp)) {
            
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Navy), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Estimated Salary", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text("৳ ${String.format(Locale.US, "%.2f", totalSalary)}", color = Gold, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // 🌟 বিস্তারিত বেতন ব্রেকডাউন
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Base", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text("৳ ${String.format(Locale.US, "%.0f", baseSalary)}", color = Color.White, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Overtime ($totalOvertimeHours hrs)", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text("৳ ${String.format(Locale.US, "%.0f", overtimeAmount)}", color = Color.White, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Night Bill", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text("৳ ${String.format(Locale.US, "%.0f", nightBillAmount)}", color = Color.White, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Present Ratio: $presentRatio%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = presentRatio / 100f,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        Text("Present: $presentCount", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Absent: $absentCount", color = Color(0xFFF44336), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Off Day: $offDayCount", color = Color(0xFFFFC107), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
