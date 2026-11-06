package com.kormi.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(mainNavController: NavController, worker: Worker?) {
    if (worker == null) return
    
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    
    var expanded by remember { mutableStateOf(false) }
    var currentPhotoUri by remember { mutableStateOf(worker.photo) }

    // গ্যালারি থেকে ছবি নেওয়ার লঞ্চার
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            currentPhotoUri = uri.toString()
            scope.launch {
                db.workerDao().updatePhoto(worker.workerId, uri.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Person, contentDescription = "Menu", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profile Settings") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = { expanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                                onClick = { 
                                    expanded = false
                                    mainNavController.navigate("login") { popUpTo(0) { inclusive = true } } 
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // প্রোফাইল ছবি
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Navy.copy(alpha = 0.1f))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (currentPhotoUri != null) {
                    AsyncImage(
                        model = currentPhotoUri,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = "Photo", tint = Navy, modifier = Modifier.size(60.dp))
                }
            }
            Text("ছবি পরিবর্তন করতে ক্লিক করুন", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(16.dp))

            Text(text = worker.fullName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(text = "Card: ${worker.workerId}", fontSize = 14.sp, color = Gold, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileRow("Phone", worker.phone)
                    ProfileRow("Unit", worker.unit)
                    ProfileRow("Section", worker.section)
                    ProfileRow("Shift", if (worker.shift.isBlank()) "N/A" else worker.shift)
                    ProfileRow("Weekly Off Day", if (worker.weeklyOffDay.isBlank()) "N/A" else worker.weeklyOffDay)
                    ProfileRow("Status", worker.status)
                    ProfileRow("Address", if (worker.presentAddress.isNullOrBlank()) "N/A" else worker.presentAddress)
                }
            }
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Text(text = value, color = Navy, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
