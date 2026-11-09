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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.kormi.app.data.GeminiOcrHelper
import com.kormi.app.data.Production
import com.kormi.app.data.SyncHelper
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionOcrScreen(navController: NavController, loggedInWorker: Worker?) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var selectedDateDb by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var selectedDateDisplay by remember { mutableStateOf(SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(Date())) }
    var selectedUnit by remember { mutableStateOf("UNIT-01") }
    var selectedShift by remember { mutableStateOf("P/A") }
    var incharge by remember { mutableStateOf("") }

    var expandedUnit by remember { mutableStateOf(false) }
    var expandedShift by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var imageBase64 by remember { mutableStateOf("") }
    
    // ফর্ম ভ্যালু
    var cd by remember { mutableStateOf("") }
    var bd by remember { mutableStateOf("") }
    var fd by remember { mutableStateOf("") }
    var sf by remember { mutableStateOf("") }
    var rf by remember { mutableStateOf("") }
    var lc by remember { mutableStateOf("") }
    var packBags by remember { mutableStateOf("") }
    var packKg by remember { mutableStateOf("") }

    // গ্যালারি
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            imageBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }

    // ক্যামেরা
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

    fun saveProduction() {
        scope.launch {
            val date = selectedDateDb
            val u = selectedUnit
            val s = selectedShift
            val inc = incharge

            val syncArray = JSONArray()

            fun saveMachine(mName: String, kgStr: String) {
                val kg = kgStr.toDoubleOrNull() ?: 0.0
                if (kg > 0.0) {
                    db.productionDao().insertProduction(Production(date=date, unit=u, shift=s, section="MACHINE", machineName=mName, weightKg=kg, inchargeName=inc, reportedBy="Admin"))
                    val jsonObj = JSONObject()
                    jsonObj.put("date", date)
                    jsonObj.put("unit", u)
                    jsonObj.put("shift", s)
                    jsonObj.put("machineName", mName)
                    jsonObj.put("weightKg", kg)
                    jsonObj.put("inchargeName", inc)
                    syncArray.put(jsonObj)
                }
            }
            
            saveMachine("C/D", cd)
            saveMachine("B/D", bd)
            saveMachine("F/D", fd)
            saveMachine("S/F", sf)
            saveMachine("R/F", rf)
            saveMachine("L/C", lc)

            val bBags = packBags.toIntOrNull() ?: 0
            val bKg = packKg.toDoubleOrNull() ?: 0.0
            if (bBags > 0 || bKg > 0.0) {
                db.productionDao().insertProduction(Production(date=date, unit=u, shift=s, section="PACKING", machineName="PACKING", weightKg=bKg, packingBags=bBags, packingWeightKg=bKg, inchargeName=inc, reportedBy="Admin"))
                val jsonObj = JSONObject()
                jsonObj.put("date", date)
                jsonObj.put("unit", u)
                jsonObj.put("shift", s)
                jsonObj.put("machineName", "PACKING")
                jsonObj.put("weightKg", bKg)
                jsonObj.put("inchargeName", inc)
                syncArray.put(jsonObj)
            }

            if (syncArray.length() > 0) {
                SyncHelper.postData("syncProduction", syncArray)
            }

            Toast.makeText(context, "প্রোডাকশন সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Production OCR", color = Color.White, fontWeight = FontWeight.Bold) },
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
            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // কনফিগারেশন
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Date: $selectedDateDisplay", fontWeight = FontWeight.Bold, color = Navy)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(value = selectedUnit, onValueChange = {}, readOnly = true, label = { Text("Unit") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth())
                            Box(modifier = Modifier.matchParentSize().clickable { expandedUnit = true })
                            DropdownMenu(expanded = expandedUnit, onDismissRequest = { expandedUnit = false }) {
                                listOf("UNIT-01", "UNIT-02", "G/F", "F/F").forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { selectedUnit = u; expandedUnit = false }) }
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(value = selectedShift, onValueChange = {}, readOnly = true, label = { Text("Shift") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth())
                            Box(modifier = Modifier.matchParentSize().clickable { expandedShift = true })
                            DropdownMenu(expanded = expandedShift, onDismissRequest = { expandedShift = false }) {
                                listOf("P/A", "P/B", "P/C").forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { selectedShift = s; expandedShift = false }) }
                            }
                        }
                    }
                    OutlinedTextField(value = incharge, onValueChange = { incharge = it }, label = { Text("Shift In-Charge") }, modifier = Modifier.fillMaxWidth())
                }
            }

            Spacer(Modifier.height(16.dp))

            // ক্যামেরা এবং গ্যালারি বাটন
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

            if (imageBase64.isNotEmpty() && cd.isEmpty()) {
                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            val prompt = """
                                Extract the 'Total Prodn' values in Kg for machines C/D, B/D, F/D, S/F, R/F, L/C from this production sheet. 
                                Also extract Packing total bags and total weight.
                                Return a JSON object with keys: "cd", "bd", "fd", "sf", "rf", "lc", "packingBags", "packingWeight".
                                Do not include any other text, just the JSON.
                            """.trimIndent()
                            
                            val aiResponse = GeminiOcrHelper.extractRawText(imageBase64, prompt)
                            
                            if (aiResponse != null) {
                                try {
                                    val cleanJson = aiResponse.trim().removePrefix("```json").removeSuffix("```").trim()
                                    val json = JSONObject(cleanJson)
                                    cd = json.optString("cd", "")
                                    bd = json.optString("bd", "")
                                    fd = json.optString("fd", "")
                                    sf = json.optString("sf", "")
                                    rf = json.optString("rf", "")
                                    lc = json.optString("lc", "")
                                    packBags = json.optString("packingBags", "")
                                    packKg = json.optString("packingWeight", "")
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
                    Text("Extract Production Data (Gemini AI)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isProcessing) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Navy)
                }
            }

            // এডিটেবল ফর্ম
            if (cd.isNotEmpty() || bd.isNotEmpty() || packBags.isNotEmpty()) {
                Text("Extracted Data (Edit if needed):", fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = cd, onValueChange = { cd = it }, label = { Text("C/D") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = bd, onValueChange = { bd = it }, label = { Text("B/D") }, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = fd, onValueChange = { fd = it }, label = { Text("F/D") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = sf, onValueChange = { sf = it }, label = { Text("S/F") }, modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = rf, onValueChange = { rf = it }, label = { Text("R/F") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = lc, onValueChange = { lc = it }, label = { Text("L/C") }, modifier = Modifier.weight(1f))
                        }
                        Divider()
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = packBags, onValueChange = { packBags = it }, label = { Text("Packing Bags") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = packKg, onValueChange = { packKg = it }, label = { Text("Packing Kg") }, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { saveProduction() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm & Save", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                }
            }
        }
    }
}
