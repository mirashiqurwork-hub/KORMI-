package com.kormi.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkersScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    
    val workers by db.workerDao().getAllWorkers().collectAsState(initial = emptyList())
    
    // Pull to Refresh স্টেট
    var isRefreshing by remember { mutableStateOf(false) }
    var syncStatus by remember { mutableStateOf("") }

    // ⚠️ এখানে আপনার Apps Script এর URL টি কোটেশন চিহ্নসহ বসাবেন
    val appsScriptUrl = "https://script.google.com/macros/s/AKfycbwlY0TWH1ogsZ43ieZIRGxl8a8Hf_dByxTQQ4JofSI9G8GnlASUDHmwwIlcUrjX9aVQjQ/exec"

    // রিফ্রেশ করার ফাংশন
    val onRefresh: () -> Unit = {
        isRefreshing = true
        syncStatus = "Syncing..."
        scope.launch {
            val result = fetchAndSaveWorkers(appsScriptUrl, db)
            syncStatus = result
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workers List", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_worker") }, 
                containerColor = Navy, 
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Worker")
            }
        }
    ) { padding ->
        
        // Material 3 এর PullToRefreshBox
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF2F4F7))
        ) {
            // LazyColumn কে fillMaxSize() দেওয়া হলো, যাতে পুরো স্ক্রিন জুড়ে টান দেওয়া যায়
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp), 
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (syncStatus.isNotEmpty()) {
                    item {
                        Text(
                            text = syncStatus, 
                            color = Navy, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                if (workers.isEmpty() && !isRefreshing) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 50.dp), 
                            contentAlignment = Alignment.Center
                        ) {
                            Text("কোনো কর্মী পাওয়া যায়নি। উপরে টান দিয়ে রিফ্রেশ করুন।", color = TextSecondary)
                        }
                    }
                } else {
                    items(workers) { worker ->
                        WorkerCard(worker, navController)
                    }
                }
            }
        }
    }
}

suspend fun fetchAndSaveWorkers(urlString: String, db: AppDatabase): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)
                
                var count = 0
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val worker = Worker(
                        workerId = obj.getString("workerId"),
                        fullName = obj.getString("fullName"),
                        role = "Worker",
                        designation = "Helper",
                        companyId = 1,
                        photo = obj.optString("photo", ""),
                        phone = obj.optString("phone", ""),
                        nidBirthCert = obj.optString("nid", ""),
                        dateOfBirth = obj.optString("dob", ""),
                        presentAddress = obj.optString("address", ""),
                        section = obj.optString("section", ""),
                        shift = obj.optString("shift", ""),
                        weeklyOffDay = obj.optString("offDay", ""),
                        status = if (obj.optString("status") == "Active") "Active" else "Inactive",
                        unit = obj.optString("unit", ""),
                        passwordHash = "1234",
                        salary = 500.0
                    )
                    db.workerDao().insertWorker(worker)
                    count++
                }
                connection.disconnect()
                "সফলভাবে $count জন কর্মীর ডাটা সিঙ্ক হয়েছে!"
            } else {
                "সিঙ্ক ব্যর্থ হয়েছে। কোড: $responseCode"
            }
        } catch (e: Exception) {
            Log.e("SyncError", e.toString())
            "সিঙ্ক এরর: ${e.message}"
        }
    }
}

@Composable
fun WorkerCard(worker: Worker, navController: NavController) {
    val context = LocalContext.current
    
    Card(
        onClick = { navController.navigate("worker_details/${worker.workerId}") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Navy.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = "Photo", tint = Navy, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = worker.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(4.dp))
                Text(text = "Card: ${worker.workerId}", fontSize = 12.sp, color = TextSecondary)
                Text(text = "Section: ${worker.section ?: "N/A"}", fontSize = 12.sp, color = TextSecondary)
            }
            IconButton(onClick = {
                if (worker.phone.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                    context.startActivity(intent)
                }
            }) {
                Icon(Icons.Default.Phone, contentDescription = "Call", tint = Navy)
            }
        }
    }
}
