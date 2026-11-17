package com.kormi.app.ui.screens

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Worker
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManpowerScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    
    var workers by remember { mutableStateOf<List<Worker>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.workerDao().getAllWorkers().collect { list ->
            workers = list
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manpower Summary") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        saveManpowerBitmapToPdf(context, workers)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "Total Active Workers: ${workers.size}", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(workers) { worker ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = worker.fullName, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "ID: ${worker.workerId} | Designation: ${worker.designation}", fontSize = 13.sp)
                            Text(text = "Shift: ${worker.shift ?: "N/A"} | Section: ${worker.section ?: "N/A"}", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun saveManpowerBitmapToPdf(context: Context, workers: List<Worker>) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        
        val canvas = page.canvas
        val paint = android.graphics.Paint().apply {
            textSize = 12f
            color = android.graphics.Color.BLACK
        }
        
        var yPosition = 25f
        canvas.drawText("Manpower Summary Report", 10f, yPosition, paint)
        yPosition += 20f
        
        for (w in workers) {
            if (yPosition > 550f) break
            canvas.drawText("${w.workerId}: ${w.fullName} (${w.designation})", 10f, yPosition, paint)
            yPosition += 15f
        }
        
        pdfDocument.finishPage(page)
        
        val file = File(context.getExternalFilesDir(null), "Manpower_Summary.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        
        Toast.makeText(context, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error saving PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
