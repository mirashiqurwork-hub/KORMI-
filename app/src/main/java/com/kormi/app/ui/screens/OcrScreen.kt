package com.kormi.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

// নতুন ডাটা ক্লাস, যেখানে সব ফিল্ড এডিট করা যাবে
data class ParsedData(
    var id: String = "",
    var name: String = "",
    var phone: String = "",
    var section: String = "BACKSIDE",
    var shift: String = "P/A",
    var unit: String = "UNIT-01"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(navController: NavController, ocrType: String, loggedInWorker: Worker?, currentUserRole: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    
    var extractedText by remember { mutableStateOf("") }
    var parsedList = remember { mutableStateListOf<ParsedData>() }
    var isProcessing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // গ্যালারি
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    extractedText = visionText.text
                    isProcessing = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "OCR Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    isProcessing = false
                }
        }
    }

    // ক্যামেরা
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            isProcessing = true
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    extractedText = visionText.text
                    isProcessing = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "OCR Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    isProcessing = false
                }
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
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F4F7))
                .padding(padding)
                .padding(16.dp)
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
                        Text("Processing Image...", color = TextSecondary)
                    }
                }
            }

            if (extractedText.isNotEmpty() && parsedList.isEmpty()) {
                Button(
                    onClick = {
                        parsedList.clear()
                        val lines = extractedText.lines()
                        
                        // সাধারণ regex যা আইডি (UPD, UCPD) বা লম্বা সংখ্যা খুঁজে বের করে
                        val idRegex = Regex("""[A-Z]{2,4}\d{5,7}""")
                        val numberRegex = Regex("""^\d{5,7}""")

                        for (line in lines) {
                            val trimmedLine = line.trim()
                            if (trimmedLine.isEmpty() || trimmedLine.contains("Employee", ignoreCase = true) || trimmedLine.contains("Name", ignoreCase = true) || trimmedLine.contains("SL", ignoreCase = true)) continue

                            val idMatch = idRegex.find(trimmedLine) ?: numberRegex.find(trimmedLine)
                            
                            if (idMatch != null) {
                                val id = idMatch.value
                                val remainingText = trimmedLine.replace(id, "").trim()
                                
                                // ডিফল্টভাবে বাকি লেখাটা নাম হিসেবে ধরবে
                                parsedList.add(ParsedData(id = id, name = remainingText))
                            } else if (trimmedLine.length > 3 && !trimmedLine.all { it.isDigit() }) {
                                // যদি কোনো আইডি না পাওয়া যায় কিন্তু টেক্সট থাকে, তবে ডামি ডাটা তৈরি করবে যাতে ইউজার এডিট করতে পারে
                                parsedList.add(ParsedData(id = "MISSING_ID", name = trimmedLine))
                            }
                        }
                        if (parsedList.isEmpty()) {
                            Toast.makeText(context, "কোনো ডাটা খুঁজে পাওয়া যায়নি! ম্যানুয়ালি এড করুন।", Toast.LENGTH_SHORT).show()
                            // ডামি রো তৈরি করা যাতে ইউজার ম্যানুয়ালি লিখতে পারে
                            parsedList.add(ParsedData())
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Extract & Edit Data", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // এডিটেবল লিস্ট
            if (parsedList.isNotEmpty()) {
                Text("Parsed Data (${parsedList.size} items) - Edit all fields:", fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(8.dp))
                
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(parsedList) { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // ID এবং Name
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = item.id,
                                        onValueChange = { parsedList[index] = item.copy(id = it) },
                                        label = { Text("ID", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                    )
                                    OutlinedTextField(
                                        value = item.name,
                                        onValueChange = { parsedList[index] = item.copy(name = it) },
                                        label = { Text("Name", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                    )
                                }

                                // Phone এবং Unit
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = item.phone,
                                        onValueChange = { parsedList[index] = item.copy(phone = it) },
                                        label = { Text("Phone", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                    )
                                    // Unit Dropdown
                                    var expandedUnit by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedTextField(
                                            value = item.unit, onValueChange = {}, readOnly = true, label = { Text("Unit", fontSize = 10.sp) },
                                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.clickable { expandedUnit = true }) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                        )
                                        Box(modifier = Modifier.matchParentSize().clickable { expandedUnit = true })
                                        DropdownMenu(expanded = expandedUnit, onDismissRequest = { expandedUnit = false }) {
                                            listOf("UNIT-01", "UNIT-02", "G/F", "F/F").forEach { u ->
                                                DropdownMenuItem(text = { Text(u) }, onClick = { parsedList[index] = item.copy(unit = u); expandedUnit = false })
                                            }
                                        }
                                    }
                                }

                                // Shift এবং Section
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Shift Dropdown
                                    var expandedShift by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedTextField(
                                            value = item.shift, onValueChange = {}, readOnly = true, label = { Text("Shift", fontSize = 10.sp) },
                                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.clickable { expandedShift = true }) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                        )
                                        Box(modifier = Modifier.matchParentSize().clickable { expandedShift = true })
                                        DropdownMenu(expanded = expandedShift, onDismissRequest = { expandedShift = false }) {
                                            listOf("P/A", "P/B", "P/C").forEach { s ->
                                                DropdownMenuItem(text = { Text(s) }, onClick = { parsedList[index] = item.copy(shift = s); expandedShift = false })
                                            }
                                        }
                                    }

                                    // Section Dropdown
                                    var expandedSection by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedTextField(
                                            value = item.section, onValueChange = {}, readOnly = true, label = { Text("Section", fontSize = 10.sp) },
                                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.clickable { expandedSection = true }) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                        )
                                        Box(modifier = Modifier.matchParentSize().clickable { expandedSection = true })
                                        DropdownMenu(expanded = expandedSection, onDismissRequest = { expandedSection = false }) {
                                            listOf("BACKSIDE", "RING", "AUTOCONE", "PACKING").forEach { s ->
                                                DropdownMenuItem(text = { Text(s) }, onClick = { parsedList[index] = item.copy(section = s); expandedSection = false })
                                            }
                                        }
                                    }
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
                                    if (data.id.isNotBlank() && data.id != "MISSING_ID" && data.name.isNotBlank()) {
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
                                Toast.makeText(context, "$savedCount জন ওয়ার্কার সফলভাবে সেভ হয়েছে!", Toast.LENGTH_LONG).show()
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
