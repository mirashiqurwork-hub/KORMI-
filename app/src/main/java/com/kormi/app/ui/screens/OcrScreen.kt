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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.kormi.app.data.GeminiOcrHelper
import com.kormi.app.data.ParsedWorker
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(navController: NavController, ocrType: String, loggedInWorker: Worker?, currentUserRole: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    
    var parsedList = remember { mutableStateListOf<ParsedWorker>() }
    var isProcessing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    // ছবির Base64 (AI-তে পাঠানোর জন্য)
    var imageBase64 by remember { mutableStateOf("") }

    // গ্যালারি থেকে ছবি নেওয়া
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            imageBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }

    // ক্যামেরা দিয়ে ছবি তোলা
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            imageBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "ক্যামেরা ব্যবহার করতে পারমিশন দিন!", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan $ocrType", color = Color.White, fontWeight = FontWeight.Bold) },
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
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

            if (isProcessing) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Navy)
                        Spacer(Modifier.height(8.dp))
                        Text("AI Data Processing...", color = TextSecondary)
                    }
                }
            }

            if (imageBase64.isNotEmpty() && parsedList.isEmpty() && !isProcessing) {
                // AI Extraction বাটন
                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            val aiResults = GeminiOcrHelper.extractWorkersFromImage(imageBase64)
                            parsedList.clear()
                            parsedList.addAll(aiResults)
                            isProcessing = false
                            if (parsedList.isEmpty()) {
                                Toast.makeText(context, "কোনো ডাটা পাওয়া যায়নি! আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)), // AI পার্পল কালার
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Extract with Gemini AI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (parsedList.isNotEmpty()) {
                Text("AI Extracted Data (${parsedList.size} items):", fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(8.dp))
                
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(parsedList) { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("ID: ${item.id} | Name: ${item.name}", fontWeight = FontWeight.Bold, color = Navy)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Unit: ${item.unit}", fontSize = 12.sp)
                                    Text("Shift: ${item.shift}", fontSize = 12.sp)
                                    Text("Section: ${item.section}", fontSize = 12.sp)
                                }
                                if (item.phone.isNotEmpty()) {
                                    Text("Phone: ${item.phone}", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (ocrType == "Worker List") {
                            isSaving = true
                            scope.launch {
                                var savedCount = 0
                                parsedList.forEach { data ->
                                    if (data.id.isNotBlank() && data.name.isNotBlank()) {
                                        val newWorker = Worker(
                                            workerId = data.id,
                                            fullName = data.name,
                                            role = "Worker",
                                            designation = "Helper",
                                            companyId = loggedInWorker?.companyId ?: 1,
                                            phone = data.phone,
                                            status = "Inactive", // ইনঅ্যাক্টিভ হিসেবে সেভ হবে
                                            unit = data.unit,
                                            shift = data.shift,
                                            section = data.section,
                                            salary = 500.0,
                                            passwordHash = "1234"
                                        )
                                        db.workerDao().insertWorker(newWorker)
                                        savedCount++
                                    }
                                }
                                Toast.makeText(context, "$savedCount জন ওয়ার্কার সেভ হয়েছে!", Toast.LENGTH_LONG).show()
                                isSaving = false
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save to Database", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
