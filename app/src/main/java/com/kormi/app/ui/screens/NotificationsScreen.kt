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
import androidx.compose.material.icons.filled.*
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
import com.kormi.app.data.Notification
import com.kormi.app.data.NotificationHelper
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

    val currentTime = System.currentTimeMillis()
    val validNotifications = allNotifications.filter { it.expiryTimestamp == 0L || it.expiryTimestamp > currentTime }

    val filteredNotifications = validNotifications.filter { notif ->
        val userCompanyId = loggedInWorker?.companyId ?: 1
        val companyMatch = if (currentUserRole == "Admin" || currentUserRole == "ADMIN") true else (notif.companyId == userCompanyId || notif.companyId == 0)
        val unitMatch = notif.targetUnit == "ALL UNIT" || notif.targetUnit == loggedInWorker?.unit
        val roleMatch = notif.targetRole == "ALL" || notif.targetRole == currentUserRole
        companyMatch && unitMatch && roleMatch
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingNotification by remember { mutableStateOf<Notification?>(null) }
    
    var noticeTitle by remember { mutableStateOf("") }
    var noticeMessage by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }
    var targetUnit by remember { mutableStateOf("ALL UNIT") }
    var targetRole by remember { mutableStateOf("ALL") }
    var targetCompanyId by remember { mutableStateOf(0) }
    var durationStr by remember { mutableStateOf("Permanent") }

    var expandedUnit by remember { mutableStateOf(false) }
    var expandedRole by remember { mutableStateOf(false) }
    var expandedCompany by remember { mutableStateOf(false) }
    var expandedDuration by remember { mutableStateOf(false) }

    val durations = listOf("1 Hour", "24 Hours", "7 Days", "Permanent")

    fun resetForm() {
        noticeTitle = ""
        noticeMessage = ""
        isPinned = false
        targetUnit = "ALL UNIT"
        targetRole = "ALL"
        targetCompanyId = 0
        durationStr = "Permanent"
        editingNotification = null
    }

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
            if (currentUserRole == "Admin" || currentUserRole == "ADMIN" || currentUserRole == "Officer" || currentUserRole == "Management") {
                FloatingActionButton(
                    onClick = { resetForm(); showCreateDialog = true },
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
                    var showMenu by remember { mutableStateOf(false) }
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (notification.isPinned) Color(0xFFFFF8E1) else Color.White),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(notification.title, fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
                                    if (notification.isPinned) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = Gold, modifier = Modifier.size(16.dp))
                                    }
                                }
                                
                                if (currentUserRole == "Admin" || currentUserRole == "ADMIN" || notification.senderName == loggedInWorker?.fullName) {
                                    Box {
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary)
                                        }
                                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                            DropdownMenuItem(
                                                text = { Text("Edit") },
                                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                                onClick = {
                                                    editingNotification = notification
                                                    noticeTitle = notification.title
                                                    noticeMessage = notification.message
                                                    isPinned = notification.isPinned
                                                    targetUnit = notification.targetUnit
                                                    targetRole = notification.targetRole
                                                    targetCompanyId = notification.companyId
                                                    durationStr = "Permanent" 
                                                    showCreateDialog = true
                                                    showMenu = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete", color = Color.Red) },
                                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                                                onClick = {
                                                    scope.launch {
                                                        db.notificationDao().deleteNotificationById(notification.id)
                                                        Toast.makeText(context, "ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
                                                    }
                                                    showMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(text = notification.message, color = Color.DarkGray, fontSize = 14.sp)
                            
                            Spacer(Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = if (notification.isPinned) "Pinned by: ${notification.senderName}" else "By: ${notification.senderName} | ${notification.targetUnit} | ${notification.targetRole}",
                                        fontSize = 11.sp,
                                        color = if (notification.isPinned) Gold else TextSecondary,
                                        fontWeight = if (notification.isPinned) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (notification.expiryTimestamp > 0) {
                                        val expiryDate = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(notification.expiryTimestamp))
                                        Text(text = "Expires: $expiryDate", fontSize = 10.sp, color = Color.Red)
                                    } else {
                                        Text(text = "Permanent Notice", fontSize = 10.sp, color = Color.Gray)
                                    }
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

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(if (editingNotification != null) "Edit Notice" else "Create New Notice", color = Navy, fontWeight = FontWeight.Bold) },
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
                    
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = durationStr, onValueChange = {}, readOnly = true, label = { Text("Notice Validity") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth()
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { expandedDuration = true })
                        DropdownMenu(expanded = expandedDuration, onDismissRequest = { expandedDuration = false }) {
                            durations.forEach { d ->
                                DropdownMenuItem(text = { Text(d) }, onClick = { durationStr = d; expandedDuration = false })
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
                                val finalCompanyId = if (currentUserRole == "Admin" || currentUserRole == "ADMIN") targetCompanyId else (loggedInWorker?.companyId ?: 1)
                                
                                val expiryTime = when(durationStr) {
                                    "1 Hour" -> System.currentTimeMillis() + 3600000L
                                    "24 Hours" -> System.currentTimeMillis() + 86400000L
                                    "7 Days" -> System.currentTimeMillis() + 604800000L
                                    else -> 0L
                                }

                                val notif = Notification(
                                    id = editingNotification?.id ?: 0,
                                    companyId = finalCompanyId,
                                    targetUnit = targetUnit,
                                    targetRole = targetRole,
                                    title = noticeTitle.trim(),
                                    message = noticeMessage.trim(),
                                    isPinned = isPinned,
                                    senderName = editingNotification?.senderName ?: (loggedInWorker?.fullName ?: "Admin"),
                                    senderDesignation = editingNotification?.senderDesignation ?: (loggedInWorker?.designation ?: "System Admin"),
                                    timestamp = editingNotification?.timestamp ?: System.currentTimeMillis(),
                                    expiryTimestamp = expiryTime
                                )

                                db.notificationDao().insertNotification(notif)
                                
                                // 🌟 এখানে সিস্টেম নোটিফিকেশন কল করা হলো
                                NotificationHelper.showNotification(context, notif.title, notif.message)
                                
                                Toast.makeText(context, "সফলভাবে সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
                                showCreateDialog = false
                                resetForm()
                            }
                        } else {
                            Toast.makeText(context, "টাইটেল এবং বিবরণ দিন!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Text("Save Notice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; resetForm() }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
