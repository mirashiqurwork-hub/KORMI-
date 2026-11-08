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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Notifications
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
import com.kormi.app.data.Company
import com.kormi.app.data.Notification
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController, loggedInWorker: Worker?, currentUserRole: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    
    val allNotifications by db.notificationDao().getAllNotifications().collectAsState(initial = emptyList())
    val companies by db.companyDao().getAllCompanies().collectAsState(initial = emptyList())

    // নোটিফিকেশন ফিল্টার লজিক (কোম্পানি, ইউনিট এবং রোল অনুযায়ী)
    val filteredNotifications = allNotifications.filter { notif ->
        val userCompanyId = loggedInWorker?.companyId ?: 1 // অ্যাডমিনের ডিফল্ট ১, কিন্তু অ্যাডমিন সব দেখবে
        
        // কোম্পানি ম্যাচিং
        val companyMatch = if (currentUserRole == "Admin" || currentUserRole == "ADMIN") {
            true // অ্যাডমিন সব কোম্পানির নোটিশ দেখবে
        } else {
            notif.companyId == userCompanyId || notif.companyId == 0 // নির্দিষ্ট কোম্পানি বা "সব কোম্পানি" (0)
        }

        val unitMatch = notif.targetUnit == "ALL UNIT" || notif.targetUnit == loggedInWorker?.unit
        val roleMatch = notif.targetRole == "ALL" || notif.targetRole == currentUserRole
        
        companyMatch && unitMatch && roleMatch
    }

    // নোটিশ তৈরির ডায়ালগ স্টেট
    var showCreateDialog by remember { mutableStateOf(false) }
    var noticeTitle by remember { mutableStateOf("") }
    var noticeMessage by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    
    // টার্গেট অপশন স্টেট
    var targetCompanyId by remember { mutableStateOf<Int?>(null) } // অ্যাডমিনের জন্য
    var targetUnit by remember { mutableStateOf("ALL UNIT") }
    var targetRole by remember { mutableStateOf("ALL") }
    var expandedCompany by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }
    var expandedRole by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        },
        floatingActionButton = {
            // শুধুমাত্র অ্যাডমিন, অফিসার এবং ম্যানেজমেন্ট নোটিশ তৈরি করতে পারবে
            if (currentUserRole == "Admin" || currentUserRole == "ADMIN" || currentUserRole == "Officer" || currentUserRole == "Management") {
                FloatingActionButton(
                    onClick = { 
                        targetCompanyId = if (currentUserRole == "Admin" || currentUserRole == "ADMIN") 0 else loggedInWorker?.companyId
                        showCreateDialog = true 
                    },
                    containerColor = Navy,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Notice")
                }
            }
        }
    ) { padding ->
        if (filteredNotifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(8.dp))
                    Text("আপনার জন্য কোনো নোটিফিকেশন নেই", color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredNotifications) { notification ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (notification.isPinned) Color(0xFFFFF8E1) else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (notification.isPinned) androidx.compose.foundation.BorderStroke(1.dp, Gold) else null,
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notification.title,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy,
                                    fontSize = 16.sp
                                )
                                if (notification.isPinned) {
                                    Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = Gold, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(text = notification.message, color = Color.DarkGray, fontSize = 14.sp)
                            
                            Spacer(Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (notification.isPinned) {
                                    Text(
                                        text = "Pinned by: ${notification.senderName} (${notification.senderDesignation})",
                                        fontSize = 11.sp,
                                        color = Gold,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Text(
                                        text = "By: ${notification.senderName} | ${notification.targetUnit} | ${notification.targetRole}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(notification.timestamp))
                                Text(text = date, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    // নোটিশ তৈরির ডায়ালগ
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Notice", color = Navy, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = noticeTitle,
                        onValueChange = { noticeTitle = it },
                        label = { Text("Notice Title") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = noticeMessage,
                        onValueChange = { noticeMessage = it },
                        label = { Text("Notice Details") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        maxLines = 3
                    )

                    // শুধুমাত্র অ্যাডমিন কোম্পানি সিলেক্ট করতে পারবে
                    if (currentUserRole == "Admin" || currentUserRole == "ADMIN") {
                        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            val currentCompanyName = if (targetCompanyId == 0) "ALL COMPANIES" else companies.find { it.companyId == targetCompanyId }?.companyName ?: "Select Company"
                            OutlinedTextField(
                                value = currentCompanyName, onValueChange = {}, readOnly = true, label = { Text("Target Company") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth()
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { expandedCompany = true })
                            DropdownMenu(expanded = expandedCompany, onDismissRequest = { expandedCompany = false }) {
                                DropdownMenuItem(text = { Text("ALL COMPANIES") }, onClick = { targetCompanyId = 0; expandedCompany = false })
                                companies.forEach { comp ->
                                    DropdownMenuItem(text = { Text(comp.companyName) }, onClick = { targetCompanyId = comp.companyId; expandedCompany = false })
                                }
                            }
                        }
                    }
                    
                    // Target Unit Dropdown
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = targetUnit, onValueChange = {}, readOnly = true, label = { Text("Target Unit") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth()
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { expandedUnit = true })
                        DropdownMenu(expanded = expandedUnit, onDismissRequest = { expandedUnit = false }) {
                            listOf("ALL UNIT", "UNIT-01", "UNIT-02", "G/F", "F/F").forEach { u ->
                                DropdownMenuItem(text = { Text(u) }, onClick = { targetUnit = u; expandedUnit = false })
                            }
                        }
                    }

                    // Target Role Dropdown
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = targetRole, onValueChange = {}, readOnly = true, label = { Text("Target Role") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth()
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { expandedRole = true })
                        DropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                            listOf("ALL", "Worker", "Officer", "Management").forEach { r ->
                                DropdownMenuItem(text = { Text(r) }, onClick = { targetRole = r; expandedRole = false })
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                        Text("Pin this notice to top", fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noticeTitle.isNotBlank() && noticeMessage.isNotBlank()) {
                            scope.launch {
                                // অ্যাডমিন হলে সিলেক্ট করা কোম্পানি, না হলে নিজের কোম্পানি
                                val finalCompanyId = if (currentUserRole == "Admin" || currentUserRole == "ADMIN") {
                                    targetCompanyId ?: 0
                                } else {
                                    loggedInWorker?.companyId ?: 1
                                }

                                db.notificationDao().insertNotification(
                                    Notification(
                                        companyId = finalCompanyId,
                                        targetUnit = targetUnit,
                                        targetRole = targetRole,
                                        title = noticeTitle.trim(),
                                        message = noticeMessage.trim(),
                                        isPinned = isPinned,
                                        senderName = loggedInWorker?.fullName ?: "Admin",
                                        senderDesignation = loggedInWorker?.designation ?: "System Admin"
                                    )
                                )
                                Toast.makeText(context, "নোটিশ পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                                showCreateDialog = false
                                noticeTitle = ""
                                noticeMessage = ""
                                isPinned = false
                                targetUnit = "ALL UNIT"
                                targetRole = "ALL"
                            }
                        } else {
                            Toast.makeText(context, "টাইটেল এবং বিবরণ দিন!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Text("Send Notice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
