package com.kormi.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.core.content.FileProvider
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.BackupHelper
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "রিস্টোর ফিচারটি পরবর্তী ধাপে কার্যকর হবে", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin & Access", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🌟 নতুন অ্যাপ্রুভাল বাটন
            AdminMenuCard("Pending Approvals", "Approve new user accounts", Icons.Default.SupervisorAccount) { navController.navigate("pending_approvals") }
            
            AdminMenuCard("Manage Companies", "View, edit or delete companies", Icons.Default.Business) { navController.navigate("manage_companies") }
            AdminMenuCard("Access Control", "Manage who sees which module", Icons.Default.Security) { navController.navigate("access_control") }
            AdminMenuCard("Add Company", "Register a new company", Icons.Default.AddBusiness) { navController.navigate("add_company") }
            AdminMenuCard("Manage Passwords", "Change worker login passwords", Icons.Default.Lock) { navController.navigate("admin_passwords") }
            AdminMenuCard("Audit Logs", "View all admin activities", Icons.Default.History) { navController.navigate("audit_logs") }
            
            AdminMenuCard("Backup Data", "Export all data to a file", Icons.Default.Backup) {
                scope.launch {
                    val backupData = BackupHelper.generateBackup(db)
                    if (backupData.isNotEmpty()) {
                        val file = File(context.cacheDir, "kormi_backup.json")
                        file.writeText(backupData)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Save Backup to..."))
                    } else {
                        Toast.makeText(context, "ব্যাকআপ তৈরি ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            AdminMenuCard("Restore Data", "Import data from a backup file", Icons.Default.Restore) {
                restoreLauncher.launch("application/json")
            }
        }
    }
}

@Composable
fun AdminMenuCard(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Navy.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = Navy)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Navy)
                Text(description, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}
