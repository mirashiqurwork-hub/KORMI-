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
import androidx.compose.material.icons.filled.*
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
    
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    // স্ট্যাটাস এবং সেকশন স্টেট
    var selectedStatus by remember { mutableStateOf("Active") }
    var selectedSection by remember { mutableStateOf("ALL") }
    
    val sections = listOf("ALL", "BACKSIDE", "RING", "AUTOCONE", "PACKING")

    val appsScriptUrl = "আপনার_স্ক্রিপ্ট_URL_এখানে_বসান"

    // ফিল্টার লজিক
    val filteredWorkers = allWorkers.filter { worker ->
        val matchesSearch = worker.fullName.contains(searchQuery, ignoreCase = true) ||
                worker.workerId.contains(searchQuery) ||
                worker.phone.contains(searchQuery)
        
        val matchesStatus = worker.status.equals(selectedStatus, ignoreCase = true)
        val matchesSection = selectedSection == "ALL" || worker.section.equals(selectedSection, ignoreCase = true)

        val userRole = if (currentUserRole == "ADMIN") "Admin" else currentUserRole
        val roleMatch = if (userRole == "Admin") true else {
            if (loggedInWorker?.role == "Officer") {
                val unitMatch = worker.unit == loggedInWorker.unit || loggedInWorker.unit == "ALL UNIT"
                val secMatch = worker.section == loggedInWorker.section || loggedInWorker.section == "ALL SECTION"
                val shiftMatch = worker.shift == loggedInWorker.shift || loggedInWorker.shift == "ALL SHIFT"
                unitMatch && secMatch && shiftMatch
            } else if (loggedInWorker?.role == "Management") {
                worker.unit == loggedInWorker.unit || loggedInWorker.unit == "ALL UNIT"
            } else true
        }

        matchesSearch && matchesStatus && matchesSection && roleMatch
    }

    val onRefresh: () -> Unit = {
        isRefreshing = true
        scope.launch {
            val result = fetchAndSaveWorkers(appsScriptUrl, db)
            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Workers List", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
                )
                // Active/Inactive Tabs
                TabRow(
                    selectedTabIndex = if (selectedStatus == "Active") 0 else 1,
                    containerColor = Navy,
                    contentColor = Color.White
                ) {
                    Tab(
                        selected = selectedStatus == "Active",
                        onClick = { selectedStatus = "Active" },
                        text = { Text("Active") }
                    )
                    Tab(
                        selected = selectedStatus == "Inactive",
                        onClick = { selectedStatus = "Inactive" },
                        text = { Text("Inactive") }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentUserRole == "Admin" || currentUserRole == "ADMIN") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // OCR FAB
                    FloatingActionButton(
                        onClick = { navController.navigate("ocr_worker") },
                        containerColor = Color(0xFF388E3C),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Add with OCR")
                    }
                    // Manual Add FAB
                    FloatingActionButton(
                        onClick = { navController.navigate("add_worker") }, 
                        containerColor = Navy, 
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Worker")
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF2F4F7))) {
            
            // সার্চ বার
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Name, ID, Phone...", fontSize = 14.sp) },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Navy) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                singleLine = true
            )

            // সেকশন ফিল্টার (Scrollable)
            ScrollableTabRow(
                selectedTabIndex = sections.indexOf(selectedSection),
                containerColor = Color.White,
                contentColor = Navy,
                edgePadding = 16.dp
            ) {
                sections.forEach { section ->
                    Tab(
                        selected = selectedSection == section,
                        onClick = { selectedSection = section },
                        text = { Text(section, fontSize = 12.sp) }
                    )
                }
            }

            // লিস্ট ভিউ
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
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                                Text("এই ক্যাটাগরিতে কোনো কর্মী পাওয়া যায়নি।", color = TextSecondary)
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
                    val existing = db.workerDao().getWorkerByCard(obj.getString("workerId"))
                    
                    val worker = Worker(
                        workerId = obj.getString("workerId"),
                        fullName = obj.getString("fullName"),
                        role = existing?.role ?: "Worker",
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
                Text(text = "Sec: ${worker.section ?: "N/A"} | Status: ${worker.status}", fontSize = 12.sp, color = TextSecondary)
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
