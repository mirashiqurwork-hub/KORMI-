package com.kormi.app.ui.screens

import android.widget.Toast
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
fun AccessControlScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val allWorkers by db.workerDao().getAllWorkers().collectAsState(initial = emptyList())
    
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val allModules = listOf("Workers", "Attendance", "Manpower", "Production", "Breakdown", "Messages", "Location", "Notifications")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Access Control", color = Color.White, fontWeight = FontWeight.Bold) },
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
            items(allWorkers) { worker ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        selectedWorker = worker
                        showDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(worker.fullName, fontWeight = FontWeight.Bold, color = Navy)
                        Text("Card: ${worker.workerId} | Role: ${worker.role}", fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text("Allowed Modules: ${worker.allowedModules}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        if (showDialog && selectedWorker != null) {
            val currentAllowed = selectedWorker!!.allowedModules.split(",").map { it.trim() }.toMutableList()
            
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Manage Access: ${selectedWorker!!.fullName}", fontSize = 16.sp, color = Navy, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        allModules.forEach { module ->
                            val isChecked = currentAllowed.contains(module)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (isChecked) currentAllowed.remove(module) else currentAllowed.add(module)
                                }.padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (it) currentAllowed.add(module) else currentAllowed.remove(module)
                                    }
                                )
                                Text(module, fontSize = 14.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val updatedModules = currentAllowed.joinToString(",")
                                val updatedWorker = selectedWorker!!.copy(allowedModules = updatedModules)
                                db.workerDao().updateWorker(updatedWorker)
                                Toast.makeText(context, "পারমিশন আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                                showDialog = false
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
