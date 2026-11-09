package com.kormi.app.ui.screens

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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
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
import com.kormi.app.data.Message
import com.kormi.app.ui.theme.Gold
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

    // মেসেজ রিড মার্ক করা
    LaunchedEffect(Unit) {
        db.messageDao().markMessagesAsRead(otherUserId, currentUserId)
    }

    // নতুন মেসেজ আসলে নিচে স্ক্রল করা
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
        // চ্যাটের ব্যাকগ্রাউন্ড (হোয়াটসঅ্যাপের মতো হালকা প্যাটার্ন)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE5DDD5)) // হালকা বেইজ ব্যাকগ্রাউন্ড
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == currentUserId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 0.dp,
                                bottomEnd = if (isMe) 0.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) Color(0xFFDCF8C6) else Color.White // হোয়াটসঅ্যাপ স্টাইল কালার
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = msg.messageText,
                                    color = Color.Black,
                                    fontSize = 15.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                                    Text(text = time, fontSize = 10.sp, color = Color.Gray)
                                    
                                    if (isMe) {
                                        Spacer(Modifier.width(4.dp))
                                        if (msg.isRead) {
                                            // ডাবল টিক (নীল রঙের)
                                            Icon(Icons.Default.DoneAll, contentDescription = "Read", tint = Color(0xFF34B7F1), modifier = Modifier.size(14.dp))
                                        } else {
                                            // সিঙ্গেল টিক (ধূসর রঙের)
                                            Icon(Icons.Default.Done, contentDescription = "Sent", tint = Color.Gray, modifier = Modifier.size(14.dp))
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
}
