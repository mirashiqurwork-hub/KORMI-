package com.kormi.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Attendance
import com.kormi.app.data.GeminiOcrHelper // ইমপোর্ট ঠিক করা হলো
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrAttendanceScreen(navController: NavController, loggedInWorker: Worker?) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var selectedShift by remember { mutableStateOf("P/A") }
    var selectedSection by remember { mutableStateOf("RING") }
    var listType by remember { mutableStateOf("Absent List") }

    var expandedShift by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var extractedIds = remember { mutableStateListOf<String>() }
    var imageBase64 by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            imageBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            imageBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) cameraLauncher.launch(null) else Toast.makeText(context, "পারমিশন দিন!", Toast.LENGTH_SHORT).show()
    }

    fun processAndSaveAttendance() {
        if (extractedIds.isEmpty()) {
            Toast.makeText(context, "আগে ছবি থেকে ডাটা এক্সট্রাক্ট করুন!", Toast.LENGTH_SHORT).show()
            return
        }
        isProcessing = true
        scope.launch {
            val allWorkers = db.workerDao().getAllActiveWorkersList()
            val targetWorkers = allWorkers.filter { 
                (it.section == selectedSection || it.section == "ALL SECTION") && 
                (it.shift == selectedShift || it.shift == "ALL SHIFT") 
            }

            var presentCount = 0
            var absentCount = 0

            targetWorkers.forEach { worker ->
                val isInList = extractedIds.contains(worker.workerId)
                
                val status = if (listType == "Absent List") {
                    if (isInList) "Absent" else "Present"
                } else {
                    if (isInList) "Present" else "Absent"
                }

                db.attendanceDao().insertOrUpdate(Attendance(worker.workerId, selectedDate, status))
                if (status == "Present") presentCount++ else absentCount++
            }

            isProcessing = false
            Toast.makeText(context, "হাজিরা সেভ হয়েছে! (Present: $presentCount, Absent: $absentCount)", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance OCR", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding).padding(16.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(value = selectedShift, onValueChange = {}, readOnly = true, label = { Text("Shift") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth())
                            Box(modifier = Modifier.matchParentSize().clickable { expandedShift = true })
                            DropdownMenu(expanded = expandedShift, onDismissRequest = { expandedShift = false }) {
                                listOf("P/A", "P/B", "P/C").forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { selectedShift = s; expandedShift = false }) }
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(value = selectedSection, onValueChange = {}, readOnly = true, label = { Text("Section") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth())
                            Box(modifier = Modifier.matchParentSize().clickable { expandedSection = true })
                            DropdownMenu(expanded = expandedSection, onDismissRequest = { expandedSection = false }) {
                                listOf("BACKSIDE", "RING", "AUTOCONE", "PACKING").forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { selectedSection = s; expandedSection = false }) }
                            }
                        }
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = listType, onValueChange = {}, readOnly = true, label = { Text("List Type") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth())
                        Box(modifier = Modifier.matchParentSize().clickable { expandedType = true })
                        DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                            listOf("Absent List", "Present List").forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { listType = t; expandedType = false }) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch(null) else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Camera", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Gallery", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (imageBase64.isNotEmpty() && extractedIds.isEmpty()) {
                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            val prompt = "Extract all Employee Card Numbers (IDs) from this image. Return a JSON array of strings containing only the IDs."
                            val aiResponse = GeminiOcrHelper.extractRawText(imageBase64, prompt)

                            if (aiResponse != null) {
                                try {
                                    val cleanJson = aiResponse.trim().removePrefix("```json").removeSuffix("```").trim()
                                    val jsonArray = org.json.JSONArray(cleanJson) // JSONArray ঠিক করা হলো
                                    for (i in 0 until jsonArray.length()) {
                                        extractedIds.add(jsonArray.getString(i))
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "ডাটা পড়তে সমস্যা হয়েছে!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "AI থেকে ডাটা পাওয়া যায়নি!", Toast.LENGTH_SHORT).show()
                            }
                            isProcessing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Extract IDs (Gemini AI)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isProcessing) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Navy)
                }
            }

            if (extractedIds.isNotEmpty()) {
                Text("Found ${extractedIds.size} IDs", fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(extractedIds) { id ->
                        Text("ID: $id", modifier = Modifier.padding(vertical = 4.dp), fontSize = 14.sp)
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { processAndSaveAttendance() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm & Save Attendance", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                }
            }
        }
    }
}
