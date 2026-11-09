package com.kormi.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material3.*
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
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHierarchyScreen(navController: NavController, status: String, loggedInWorker: Worker?, currentUserRole: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val allWorkers by db.workerDao().getAllWorkers().collectAsState(initial = emptyList())

    var selectedUnit by remember { mutableStateOf<String?>(null) }
    var selectedSection by remember { mutableStateOf<String?>(null) }

    val baseWorkers = allWorkers.filter { worker ->
        val roleMatch = if (currentUserRole == "Admin" || currentUserRole == "ADMIN") true else {
            if (loggedInWorker?.role == "Officer") {
                val unitMatch = worker.unit == loggedInWorker.unit || loggedInWorker.unit == "ALL UNIT"
                val secMatch = worker.section == loggedInWorker.section || loggedInWorker.section == "ALL SECTION"
                val shiftMatch = worker.shift == loggedInWorker.shift || loggedInWorker.shift == "ALL SHIFT"
                unitMatch && secMatch && shiftMatch
            } else if (loggedInWorker?.role == "Management") {
                worker.unit == loggedInWorker.unit || loggedInWorker.unit == "ALL UNIT"
            } else true
        }
        roleMatch && worker.status.equals(status, ignoreCase = true)
    }

    val units = listOf("UNIT-01", "UNIT-02", "G/F", "F/F")
    val sections = listOf("BACKSIDE", "RING", "AUTOCONE", "PACKING")

    val currentList = when {
        selectedUnit == null -> units
        selectedSection == null -> sections
        else -> baseWorkers.filter { it.unit == selectedUnit && it.section == selectedSection }.map { it.fullName }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when {
                            selectedUnit == null -> "$status Units"
                            selectedSection == null -> "$selectedUnit Sections"
                            else -> "$selectedSection Workers"
                        }, 
                        color = Color.White, fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (selectedSection != null) selectedSection = null
                        else if (selectedUnit != null) selectedUnit = null
                        else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                        Text("কোনো ডাটা পাওয়া যায়নি।", color = TextSecondary)
                    }
                }
            } else {
                if (selectedUnit == null) {
                    items(units) { unit ->
                        val count = baseWorkers.count { it.unit == unit }
                        HierarchyCard(title = unit, subtitle = "$count Workers", onClick = { selectedUnit = unit })
                    }
                } else if (selectedSection == null) {
                    items(sections) { section ->
                        val count = baseWorkers.count { it.unit == selectedUnit && it.section == section }
                        HierarchyCard(title = section, subtitle = "$count Workers", onClick = { selectedSection = section })
                    }
                } else {
                    val workersInSection = baseWorkers.filter { it.unit == selectedUnit && it.section == selectedSection }
                    items(workersInSection) { worker ->
                        HierarchyWorkerCard(worker, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun HierarchyCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Navy.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Business, contentDescription = null, tint = Navy)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

// এখানে নাম পরিবর্তন করা হলো
@Composable
fun HierarchyWorkerCard(worker: Worker, navController: NavController) {
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
                Text(text = "Phone: ${if (worker.phone.isBlank()) "N/A" else worker.phone}", fontSize = 12.sp, color = TextSecondary)
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
