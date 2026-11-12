package com.kormi.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Message
import com.kormi.app.ui.theme.Navy
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(navController: NavController, currentUserId: String, otherUserId: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var messageText by remember { mutableStateOf("") }
    val messages by db.messageDao().getChatMessages(currentUserId, otherUserId).collectAsState(initial = emptyList())
    val listState = rememberLazyListState()
    
    var showMenu by remember { mutableStateOf(false) }
    var isDarkWallpaper by remember { mutableStateOf(false) }

    val roomName = "kormi_call_${minOf(currentUserId, otherUserId)}_${maxOf(currentUserId, otherUserId)}"

    // 🌟 ছবি পাঠানোর লঞ্চার
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val msg = Message(
                    senderId = currentUserId,
                    receiverId = otherUserId,
                    messageText = "",
                    imageUri = uri.toString(),
                    isRead = false
                )
                db.messageDao().insertMessage(msg)
            }
        }
    }

    LaunchedEffect(Unit) {
        db.messageDao().markMessagesAsRead(otherUserId, currentUserId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (otherUserId == "ADMIN") "Admin (Sir)" else "Worker: $otherUserId", 
                        color = Color.White, fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("call/$roomName/audio") }) {
                        Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = Color.White)
                    }
                    IconButton(onClick = { navController.navigate("call/$roomName/video") }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(if (isDarkWallpaper) "Light Wallpaper" else "Dark Wallpaper") },
                                onClick = { 
                                    isDarkWallpaper = !isDarkWallpaper
                                    showMenu = false 
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🌟 গ্যালারি থেকে ছবি সিলেক্ট করার বাটন
                IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Image, contentDescription = "Attach Image", tint = Navy)
                }
                
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF2F4F7),
                        unfocusedContainerColor = Color(0xFFF2F4F7),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            scope.launch {
                                val msg = Message(
                                    senderId = currentUserId,
                                    receiverId = otherUserId,
                                    messageText = messageText,
                                    isRead = false
                                )
                                db.messageDao().insertMessage(msg)
                                messageText = ""
                            }
                        }
                    },
                    modifier = Modifier.background(Navy, CircleShape).size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkWallpaper) Color(0xFF1E1E1E) else Color(0xFFE5DDD5))
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == currentUserId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp, topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 0.dp,
                                bottomEnd = if (isMe) 0.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) Color(0xFFDCF8C6) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                // 🌟 যদি মেসেজে ছবি থাকে, তবে ছবি দেখাবে
                                if (msg.imageUri != null) {
                                    AsyncImage(
                                        model = msg.imageUri,
                                        contentDescription = "Sent Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(200.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }

                                // টেক্সট মেসেজ থাকলে দেখাবে
                                if (msg.messageText.isNotEmpty()) {
                                    Text(text = msg.messageText, color = Color.Black, fontSize = 15.sp)
                                }

                                Spacer(Modifier.height(4.dp))
                                Row(modifier = Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                                    val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                                    Text(text = time, fontSize = 10.sp, color = Color.Gray)
                                    if (isMe) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            imageVector = if (msg.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                            contentDescription = "Status",
                                            tint = if (msg.isRead) Color(0xFF34B7F1) else Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
