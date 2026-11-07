package com.kormi.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Attendance
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val workers by db.workerDao().getAllActiveWorkers().collectAsState(initial = emptyList())
    
    val calendar = Calendar.getInstance()
    var selectedDateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)) }
    
    var attendanceState by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isSaved by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDateStr, workers) {
        if (workers.isNotEmpty()) {
            val existingList = db.attendanceDao().getAttendanceByDate(selectedDateStr)
            val existingMap = existingList.associate { it.workerId to it.status }
            
            val newMap = mutableMapOf<String, String>()
            val dayOfWeek = getDayOfWeek(selectedDateStr)
            
            workers.forEach { worker ->
                if (existingMap.containsKey(worker.workerId)) {
                    newMap[worker.workerId] = existingMap[worker.workerId]!!
                } else {
                    newMap[worker.workerId] = if (worker.weeklyOffDay.equals(dayOfWeek, ignoreCase = true)) "Off Day" else "Present"
                }
            }
            attendanceState = newMap
            isSaved = false
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance", color = Color.White, fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        attendanceState.forEach { (workerId, status) ->
                            db.attendanceDao().insertOrUpdate(Attendance(workerId, selectedDateStr, status))
                        }
                        isSaved = true
                    }
                },
                containerColor = Gold, contentColor = Navy
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF2F4F7))) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Date: $selectedDateStr", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                if (isSaved) { Text(text = "Saved Successfully!", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold) }
            }

            if (workers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Active Workers Found.", color = TextSecondary)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(workers) { worker ->
                        val currentStatus = attendanceState[worker.workerId] ?: "Present"
                        AttendanceRow(worker = worker, status = currentStatus, onStatusChange = { newStatus ->
                            attendanceState = attendanceState.toMutableMap().apply { this[worker.workerId] = newStatus }
                            isSaved = false
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceRow(worker: Worker, status: String, onStatusChange: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Navy.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Text(worker.fullName.take(1), color = Navy, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(worker.fullName, fontWeight = FontWeight.Bold, color = Navy)
                    Text("Card: ${worker.workerId} | Off: ${worker.weeklyOffDay ?: "N/A"}", fontSize = 11.sp, color = TextSecondary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusButton("Present", status == "Present", Color(0xFF4CAF50)) { onStatusChange("Present") }
                StatusButton("Absent", status == "Absent", Color(0xFFF44336)) { onStatusChange("Absent") }
                StatusButton("Off Day", status == "Off Day", Color(0xFFFFC107)) { onStatusChange("Off Day") }
            }
        }
    }
}

@Composable
fun RowScope.StatusButton(text: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = Modifier.weight(1f).height(36.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) color else Color.LightGray.copy(alpha = 0.3f), contentColor = if (isSelected) Color.White else Color.DarkGray),
        contentPadding = PaddingValues(0.dp), shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}

fun getDayOfWeek(dateStr: String): String {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateStr)
        val cal = Calendar.getInstance()
        cal.time = date!!
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "SATURDAY"
            Calendar.SUNDAY -> "SUNDAY"
            Calendar.MONDAY -> "MONDAY"
            Calendar.TUESDAY -> "TUESDAY"
            Calendar.WEDNESDAY -> "WEDNESDAY"
            Calendar.THURSDAY -> "THURSDAY"
            Calendar.FRIDAY -> "FRIDAY"
            else -> ""
        }
    } catch (e: Exception) { "" }
}
