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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavController, currentUserId: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val workers by db.workerDao().getAllWorkers().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }

    val filteredWorkers = workers.filter { 
        it.fullName.contains(searchQuery, ignoreCase = true) || it.workerId.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(padding)) {
            
            // সার্চ বার
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Name or ID...", fontSize = 14.sp) },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Navy) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF2F4F7),
                    unfocusedContainerColor = Color(0xFFF2F4F7),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // অ্যাডমিন কার্ড (যদি লগইন করা ইউজার কর্মী হয়)
                if (currentUserId != "ADMIN") {
                    item {
                        ChatListRow("ADMIN", "Admin (Sir)", onClick = {
                            navController.navigate("chat_detail/$currentUserId/ADMIN")
                        })
                    }
                }

                items(filteredWorkers) { worker ->
                    if (worker.workerId != currentUserId) {
                        ChatListRow(worker.workerId, worker.fullName, onClick = {
                            navController.navigate("chat_detail/$currentUserId/${worker.workerId}")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListRow(id: String, name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(Navy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Navy, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text(text = "ID: $id | Tap to chat", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}
