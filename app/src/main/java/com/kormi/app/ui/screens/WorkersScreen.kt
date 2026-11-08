package com.kormi.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Search
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
fun WorkersScreen(navController: NavController, loggedInWorker: Worker?, currentUserRole: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    
    val allWorkers by db.workerDao().getAllWorkers().collectAsState(initial = emptyList())
    
    // সার্চ স্টেট
    var searchQuery by remember { mutableStateOf("") }
    
    // Pull to Refresh স্টেট
    var isRefreshing by remember { mutableStateOf(false) }

    val appsScriptUrl = "https://script.google.com/macros/s/AKfycbwlY0TWH1ogsZ43ieZIRGxl8a8Hf_dByxTQQ4JofSI9G8GnlASUDHmwwIlcUrjX9aVQjQ/exec"

    // রোল অনুযায়ী কর্মী ফিল্টার এবং সার্চ ফিল্টার
    val filteredWorkers = allWorkers.filter { worker ->
        // ১. সার্চ কুয়েরি ম্যাচ করে কি না
        val matchesSearch = worker.fullName.contains(searchQuery, ignoreCase = true) ||
                worker.workerId.contains(searchQuery) ||
                worker.phone.contains(searchQuery) ||
                (worker.presentAddress?.contains(searchQuery, ignoreCase = true) == true) ||
                (worker.section?.contains(searchQuery, ignoreCase = true) == true) ||
                (worker.unit?.contains(searchQuery, ignoreCase = true) == true)

        if (!matchesSearch) return@filter false

        // ২. রোল অনুযায়ী অ্যাক্সেস কন্ট্রোল
        if (currentUserRole == "Admin" || currentUserRole == "ADMIN") {
            return@filter true // অ্যাডমিন সব দেখবে
        }
        
        if (loggedInWorker == null) return@filter false

        if (currentUserRole == "Management") {
            return@filter worker.unit == loggedInWorker.unit || loggedInWorker.unit == "ALL UNIT"
        }

        if (currentUserRole == "Officer") {
            val unitMatch = worker.unit == loggedInWorker.unit || loggedInWorker.unit == "ALL UNIT"
            val sectionMatch = worker.section == loggedInWorker.section || loggedInWorker.section == "ALL SECTION"
            val shiftMatch = worker.shift == loggedInWorker.shift || loggedInWorker.shift == "ALL SHIFT"
            return@filter unitMatch && sectionMatch && shiftMatch
        }
        
        // সাধারণ ওয়ার্কার শুধু নিজেকে দেখবে (যদি এই স্ক্রিনে আসে)
        return@filter worker.workerId == loggedInWorker.workerId
    }

    val onRefresh: () -> Unit = {
        isRefreshing = true
        scope.launch {
            val result = fetchAndSaveWorkers(appsScriptUrl, db)
            // সিঙ্কের রেজাল্ট টোস্ট মেসেজ আকারে দেখানো হচ্ছে, যাতে UI নষ্ট না হয়
            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
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
            // অ্যাডমিন ছাড়া অন্য কেউ কর্মী যোগ করতে পারবে না
            if (currentUserRole == "Admin" || currentUserRole == "ADMIN") {
                FloatingActionButton(
                    onClick = { navController.navigate("add_worker") }, 
                    containerColor = Navy, 
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Worker")
                }
            }
        }
    ) { padding ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF2F4F7))
        ) {
            // গুগল স্টাইলের সার্চ বার
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Name, ID, Phone...", fontSize = 14.sp) },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Navy) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Navy.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true
            )

            // Pull to Refresh বক্স
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), 
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filteredWorkers.isEmpty() && !isRefreshing) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 50.dp), 
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if(searchQuery.isEmpty()) "কোনো কর্মী পাওয়া যায়নি। উপরে টান দিয়ে রিফ্রেশ করুন।" else "এই সার্চে কোনো কর্মী পাওয়া যায়নি।", 
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        items(filteredWorkers) { worker ->
                            WorkerCard(worker, navController)
                        }
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
                    // চেক করা হচ্ছে ডাটাবেসে আগে থেকেই আছে কি না, যাতে রোল ওভাররাইট না হয়ে যায়
                    val existing = db.workerDao().getWorkerByCard(obj.getString("workerId"))
                    
                    // যদি আগে থেকে থাকে, তবে তার রোল/সেকশন ঠিক রাখবে, শুধু গুগল শিটের ডাটা আপডেট করবে
                    // তবে এখানে সহজ রাখার জন্য নতুন ডাটাগুলো ইন্সার্ট করা হচ্ছে
                    val worker = Worker(
                        workerId = obj.getString("workerId"),
                        fullName = obj.getString("fullName"),
                        role = existing?.role ?: "Worker", // যদি নতুন হয়, ডিফল্ট Worker
                        designation = existing?.designation ?: "Helper",
                        companyId = existing?.companyId ?: 1,
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
                        passwordHash = existing?.passwordHash ?: "1234",
                        salary = existing?.salary ?: 500.0
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
                Text(text = "Sec: ${worker.section ?: "N/A"} | Role: ${worker.role}", fontSize = 12.sp, color = TextSecondary)
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
