package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkersScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    
    // Flow থেকে ডাটা সংগ্রহ করা
    val workers by db.workerDao().getAllWorkers().collectAsState(initial = emptyList())

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
        if (workers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("কোনো কর্মী পাওয়া যায়নি। + বাটনে ক্লিক করে যোগ করুন।", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(workers) { worker ->
                    WorkerCard(worker)
                }
            }
        }
    }
}

@Composable
fun WorkerCard(worker: Worker) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Photo
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(Navy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Photo", tint = Navy, modifier = Modifier.size(30.dp))
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(text = worker.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(4.dp))
                Text(text = "Card: ${worker.workerId}", fontSize = 12.sp, color = TextSecondary)
                Text(text = "Section: ${worker.section}", fontSize = 12.sp, color = TextSecondary)
            }
            
            // Call Icon
            IconButton(onClick = { /* Call Intent goes here later */ }) {
                Icon(Icons.Default.Phone, contentDescription = "Call", tint = Navy)
            }
        }
    }
}
