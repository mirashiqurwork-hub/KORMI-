package com.kormi.app.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ManpowerScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val graphicsLayer = rememberGraphicsLayer()

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

    val datePickerDialog = DatePickerDialog(
        context, { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    fun shareImage() {
        scope.launch {
            try {
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                // 🌟 এখানে ProductionHistoryScreen-এর saveBitmapToCache ফাংশনটি কল হচ্ছে
                val uri = saveBitmapToCache(context, bitmap)
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Manpower Summary"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                    IconButton(onClick = { shareImage() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Date", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                }
        ) {
            Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
                
                Text(text = "Kormi", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text(text = "Manpower Report", fontSize = 14.sp, color = Gold, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Text(text = "Date: $selectedDateStr", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(16.dp))

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
}

@Composable
fun SummaryCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F7)), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun SectionRow(section: String, count: Int) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F7))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = section, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Navy)
            Text(text = "$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gold)
        }
    }
}
