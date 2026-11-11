package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.kormi.app.data.AuditLog
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val allWorkers by db.workerDao().getAllWorkers().collectAsState(initial = emptyList())
    val pendingWorkers = allWorkers.filter { it.status.equals("Pending", ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pending Approvals", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        if (pendingWorkers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("কোনো পেন্ডিং অ্যাকাউন্ট নেই।", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingWorkers) { worker ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                Text("Card: ${worker.workerId} | Role: ${worker.role}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        val updatedWorker = worker.copy(status = "Active")
                                        db.workerDao().updateWorker(updatedWorker)
                                        
                                        val sharedPref = context.getSharedPreferences("KormiPrefs", Context.MODE_PRIVATE)
                                        val currentUserId = sharedPref.getString("userId", "ADMIN") ?: "ADMIN"
                                        db.auditLogDao().insertLog(
                                            AuditLog(
                                                action = "Worker Approved",
                                                details = "Approved ${worker.fullName} (${worker.workerId})",
                                                performedBy = currentUserId
                                            )
                                        )
                                        android.widget.Toast.makeText(context, "অ্যাকাউন্ট অ্যাপ্রুভ হয়েছে!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Approve", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Approve")
                            }
                        }
                    }
                }
            }
        }
    }
}
