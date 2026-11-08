package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePasswordsScreen(navController: NavController, loggedInWorker: Worker?) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val allWorkers by db.workerDao().getAllWorkers().collectAsState(initial = emptyList())
    
    val filteredWorkers = if (loggedInWorker == null || loggedInWorker.role == "Admin") {
        allWorkers
    } else if (loggedInWorker.role == "Management") {
        allWorkers.filter { it.unit == loggedInWorker.unit || loggedInWorker.unit == "ALL UNIT" }
    } else {
        allWorkers.filter { 
            (it.unit == loggedInWorker.unit || loggedInWorker.unit == "ALL UNIT") &&
            (it.section == loggedInWorker.section || loggedInWorker.section == "ALL SECTION") &&
            (it.shift == loggedInWorker.shift || loggedInWorker.shift == "ALL SHIFT")
        }
    }
    
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var newPassword by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Passwords", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredWorkers) { worker ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        selectedWorker = worker
                        newPassword = ""
                        showDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(worker.fullName, fontWeight = FontWeight.Bold, color = Navy)
                            Text("Card: ${worker.workerId}", fontSize = 12.sp, color = TextSecondary)
                        }
                        Text("Change", color = Navy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showDialog && selectedWorker != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Change Password") },
                text = {
                    Column {
                        Text("Worker: ${selectedWorker!!.fullName}", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPassword.isNotBlank()) {
                                scope.launch {
                                    db.workerDao().updatePassword(selectedWorker!!.workerId, newPassword)
                                    showDialog = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
