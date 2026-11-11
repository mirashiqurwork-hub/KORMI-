package com.kormi.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.MoreTime
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
import com.kormi.app.data.*
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
fun AttendanceScreen(navController: NavController, loggedInWorker: Worker?) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val allWorkers by db.workerDao().getAllActiveWorkers().collectAsState(initial = emptyList())
    
    var selectedSection by remember { mutableStateOf("ALL") }
    val sections = listOf("ALL", "BACKSIDE", "RING", "AUTOCONE", "PACKING")

    // কেস-ইনসেনসিটিভ ফিল্টার
    val filteredWorkers = remember(allWorkers, loggedInWorker, selectedSection) {
        var list = if (loggedInWorker == null || loggedInWorker.role == "Admin") {
            allWorkers
        } else if (loggedInWorker.role == "Management") {
            allWorkers.filter { it.unit.equals(loggedInWorker.unit, ignoreCase = true) || loggedInWorker.unit == "ALL UNIT" }
        } else {
            allWorkers.filter { 
                (it.unit.equals(loggedInWorker.unit, ignoreCase = true) || loggedInWorker.unit == "ALL UNIT") &&
                (it.section.equals(loggedInWorker.section, ignoreCase = true) || loggedInWorker.section == "ALL SECTION") &&
                (it.shift.equals(loggedInWorker.shift, ignoreCase = true) || loggedInWorker.shift == "ALL SHIFT")
            }
        }
        if (selectedSection != "ALL") {
            list = list.filter { it.section.equals(selectedSection, ignoreCase = true) }
        }
        list
    }
    
    val calendar = Calendar.getInstance()
    var selectedDateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)) }
    
    var attendanceState by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isSaved by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDateStr, filteredWorkers) {
        if (filteredWorkers.isNotEmpty()) {
            val existingList = db.attendanceDao().getAttendanceByDate(selectedDateStr)
            val existingMap = existingList.associate { it.workerId to it.status }
            
            val newMap = mutableMapOf<String, String>()
            val dayOfWeek = getDayOfWeek(selectedDateStr)
            
            filteredWorkers.forEach { worker ->
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
        context, { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Attendance", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("overtime_entry") }) {
                            Icon(Icons.Default.MoreTime, contentDescription = "Overtime", tint = Gold)
                        }
                        IconButton(onClick = { navController.navigate("ocr_attendance") }) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = "OCR", tint = Color.White)
                        }
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Date", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
                )
                ScrollableTabRow(
                    selectedTabIndex = sections.indexOf(selectedSection),
                    containerColor = Navy,
                    contentColor = Color.White,
                    edgePadding = 0.dp
                ) {
                    sections.forEach { section ->
                        Tab(
                            selected = selectedSection == section,
                            onClick = { selectedSection = section },
                            text = { Text(section, color = if(selectedSection==section) Gold else Color.White, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        var presentCount = 0
                        var absentCount = 0
                        var offDayCount = 0
                        val syncArray = JSONArray()

                        attendanceState.forEach { (workerId, status) ->
                            db.attendanceDao().insertOrUpdate(Attendance(workerId, selectedDateStr, status))
                            when(status) {
                                "Present" -> presentCount++
                                "Absent" -> absentCount++
                                "Off Day" -> offDayCount++
                            }
                            val jsonObj = JSONObject()
                            jsonObj.put("date", selectedDateStr)
                            jsonObj.put("workerId", workerId)
                            jsonObj.put("status", status)
                            syncArray.put(jsonObj)
                        }
                        
                        SyncHelper.postData("syncAttendance", syncArray)

                        if (loggedInWorker != null) {
                            val notifMessage = "ইউনিট: ${loggedInWorker.unit}, শিফট: ${loggedInWorker.shift}, সেকশন: ${loggedInWorker.section}\nপ্রেজেন্ট: $presentCount জন, অ্যাবসেন্ট: $absentCount জন, অফ-ডে: $offDayCount জন।"
                            db.notificationDao().insertNotification(
                                Notification(
                                    companyId = loggedInWorker.companyId,
                                    targetUnit = loggedInWorker.unit ?: "ALL UNIT",
                                    targetRole = "Management",
                                    title = "Attendance Submitted by ${loggedInWorker.fullName}",
                                    message = notifMessage,
                                    senderName = loggedInWorker.fullName,
                                    senderDesignation = loggedInWorker.designation,
                                    isPinned = false
                                )
                            )
                        }
                        isSaved = true
                        android.widget.Toast.makeText(context, "এটেনডেন্স সেভ হয়েছে!", android.widget.Toast.LENGTH_SHORT).show()
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
                if (isSaved) { Text(text = "Saved!", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold) }
            }

            if (filteredWorkers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("আপনার সেকশনে কোনো কর্মী পাওয়া যায়নি।", color = TextSecondary)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredWorkers) { worker ->
                        val currentStatus = attendanceState[worker.workerId] ?: "Present"
                        AttendanceRow(
                            worker = worker, 
                            status = currentStatus, 
                            onStatusChange = { newStatus ->
                                attendanceState = attendanceState.toMutableMap().apply { this[worker.workerId] = newStatus }
                                isSaved = false
                            },
                            onClick = {
                                // 🌟 কার্ডে ক্লিক করলে ওয়ার্কারের প্রোফাইলে যাবে
                                navController.navigate("worker_details/${worker.workerId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceRow(worker: Worker, status: String, onStatusChange: (String) -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }, 
        shape = RoundedCornerShape(12.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Navy.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Text(worker.fullName.take(1), color = Navy, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(worker.fullName, fontWeight = FontWeight.Bold, color = Navy)
                    Text("Card: ${worker.workerId} | Sec: ${worker.section ?: "N/A"}", fontSize = 11.sp, color = TextSecondary)
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
