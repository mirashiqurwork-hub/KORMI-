package com.kormi.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary

data class ParsedData(val id: String, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(navController: NavController, ocrType: String) {
    val context = LocalContext.current
    var extractedText by remember { mutableStateOf("") }
    var parsedList by remember { mutableStateOf<List<ParsedData>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

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
            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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

            Spacer(Modifier.height(24.dp))

            if (isProcessing) {
                CircularProgressIndicator(color = Navy)
                Spacer(Modifier.height(8.dp))
                Text("Processing Image...", color = TextSecondary)
            }

            if (extractedText.isNotEmpty()) {
                Text("Raw Text (Edit if needed):", fontWeight = FontWeight.Bold, color = Navy, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = extractedText,
                    onValueChange = { extractedText = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 300.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
                
                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        // পার্সিং লজিক: Regex দিয়ে আইডি এবং নাম আলাদা করা
                        val lines = extractedText.lines()
                        val tempList = mutableListOf<ParsedData>()
                        
                        // Regex যা UCPD, UPD, UQC ইত্যাদি দিয়ে শুরু হওয়া আইডি খুঁজে বের করবে
                        val idRegex = Regex("""[A-Z]{2,4}\d{5,7}""") 
                        // সাধারণ সংখ্যার আইডির জন্য (যেমন 503174)
                        val numberRegex = Regex("""^\d{5,7}$""")

                        for (line in lines) {
                            val trimmedLine = line.trim()
                            if (trimmedLine.isEmpty()) continue

                            // ১. অক্ষর+সংখ্যা আইডি খোঁজা
                            val idMatch = idRegex.find(trimmedLine)
                            if (idMatch != null) {
                                val id = idMatch.value
                                val name = trimmedLine.replace(id, "").replace(Regex("""^\d+\s*"""), "").trim()
                                tempList.add(ParsedData(id, name))
                                continue
                            }

                            // ২. শুধু সংখ্যার আইডি খোঁজা
                            val parts = trimmedLine.split(" ")
                            for (part in parts) {
                                if (numberRegex.matches(part) && part.length >= 5) {
                                    val id = part
                                    val name = trimmedLine.replace(id, "").replace(Regex("""^\d+\s*"""), "").trim()
                                    if (name.isNotEmpty()) {
                                        tempList.add(ParsedData(id, name))
                                        break
                                    }
                                }
                            }
                        }
                        parsedList = tempList
                        if (tempList.isEmpty()) {
                            Toast.makeText(context, "কোনো আইডি খুঁজে পাওয়া যায়নি!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Extract IDs & Names", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // পার্স করা লিস্ট দেখানো
            if (parsedList.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text("Parsed Data (${parsedList.size} items):", fontWeight = FontWeight.Bold, color = Navy, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
                
                parsedList.forEach { data ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = data.id, fontWeight = FontWeight.Bold, color = Navy)
                            Text(text = data.name, color = TextSecondary)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        Toast.makeText(context, "পরবর্তী ধাপে ডাটাবেসে সেভ হবে", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save to Database", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
