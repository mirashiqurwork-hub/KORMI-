package com.kormi.app.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PictureAsPdf
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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ProductionHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val graphicsLayer = rememberGraphicsLayer()
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)) }
    
    var selectedUnit by remember { mutableStateOf<String?>(null) }
    var selectedShift by remember { mutableStateOf<String?>(null) }

    val units = listOf("UNIT-01", "UNIT-02", "G/F", "F/F")
    val shifts = listOf("P/A", "P/B", "P/C")

    val allProduction by db.productionDao().getProductionByDate(selectedDate).collectAsState(initial = emptyList())

    val datePickerDialog = DatePickerDialog(
        context, { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            selectedUnit = null
            selectedShift = null
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    fun sharePdf() {
        scope.launch {
            try {
                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                val uri = saveBitmapToPdf(context, bitmap)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Production Report"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when {
                            selectedUnit == null -> "Production Units"
                            selectedShift == null -> "$selectedUnit Shifts"
                            else -> "$selectedUnit - $selectedShift"
                        }, 
                        color = Color.White, fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (selectedShift != null) selectedShift = null
                        else if (selectedUnit != null) selectedUnit = null
                        else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { sharePdf() }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Share PDF", tint = Color.White)
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
                Text("Kormi", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text("Production Report", fontSize = 14.sp, color = Gold, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Text("Date: $selectedDate", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedUnit == null) {
                        items(units) { unit ->
                            val unitData = allProduction.filter { it.unit == unit }
                            val totalKg = unitData.sumOf { it.weightKg }
                            MenuCard(title = unit, subtitle = "Total Production: $totalKg Kg", onClick = { selectedUnit = unit })
                        }
                    } else if (selectedShift == null) {
                        items(shifts) { shift ->
                            val shiftData = allProduction.filter { it.unit == selectedUnit && it.shift == shift }
                            val shiftTotal = shiftData.sumOf { it.weightKg }
                            MenuCard(title = shift, subtitle = "Total: $shiftTotal Kg", onClick = { selectedShift = shift })
                        }
                        
                        val unitTotalData = allProduction.filter { it.unit == selectedUnit }
                        if (unitTotalData.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(16.dp))
                                Text("Total Production (All Shifts)", fontWeight = FontWeight.Bold, color = Navy, fontSize = 18.sp)
                                Spacer(Modifier.height(8.dp))
                                
                                val machineTotals = unitTotalData.groupBy { it.machineName }
                                    .mapValues { entry -> entry.value.sumOf { it.weightKg } }
                                
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        machineTotals.forEach { (machine, total) ->
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(machine, fontWeight = FontWeight.Bold, color = Navy)
                                                Text("$total Kg", fontWeight = FontWeight.Bold, color = Gold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val finalData = allProduction.filter { it.unit == selectedUnit && it.shift == selectedShift }
                        if (finalData.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                                    Text("এই শিফটে কোনো ডাটা নেই।", color = TextSecondary)
                                }
                            }
                        } else {
                            items(finalData) { prod ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F7)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(prod.machineName, fontWeight = FontWeight.Bold, color = Navy)
                                            Text("Incharge: ${prod.inchargeName}", fontSize = 12.sp, color = TextSecondary)
                                        }
                                        Text("${prod.weightKg} Kg", fontWeight = FontWeight.Bold, color = Gold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F7))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

// 🌟 বিটম্যাপ থেকে PDF তৈরি করার হেল্পার ফাংশন
fun saveBitmapToPdf(context: Context, bitmap: Bitmap): Uri {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
    pdfDocument.finishPage(page)

    val pdfFile = File(context.cacheDir, "production_${System.currentTimeMillis()}.pdf")
    try {
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    pdfDocument.close()

    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
}
