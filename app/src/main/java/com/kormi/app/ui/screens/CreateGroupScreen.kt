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
import com.kormi.app.data.ChatGroup
import com.kormi.app.ui.theme.Navy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(navController: NavController, currentUserId: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val workers by db.workerDao().getAllWorkers().collectAsState(initial = emptyList())
    var groupName by remember { mutableStateOf("") }
    val selectedMembers = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Group", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Select Members (${selectedMembers.size})", fontWeight = FontWeight.Bold, color = Navy)
            Spacer(Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(workers) { worker ->
                    if (worker.workerId != currentUserId) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (selectedMembers.contains(worker.workerId)) {
                                    selectedMembers.remove(worker.workerId)
                                } else {
                                    selectedMembers.add(worker.workerId)
                                }
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedMembers.contains(worker.workerId),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) selectedMembers.add(worker.workerId)
                                    else selectedMembers.remove(worker.workerId)
                                }
                            )
                            Column {
                                Text(worker.fullName, fontWeight = FontWeight.Bold, color = Navy)
                                Text(worker.workerId, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (groupName.isNotBlank() && selectedMembers.isNotEmpty()) {
                        scope.launch {
                            val membersStr = (selectedMembers + currentUserId).joinToString(",")
                            db.chatGroupDao().insertGroup(
                                ChatGroup(groupName = groupName, createdBy = currentUserId, members = membersStr)
                            )
                            Toast.makeText(context, "গ্রুপ তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    } else {
                        Toast.makeText(context, "গ্রুপের নাম এবং সদস্য নির্বাচন করুন!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Group", fontWeight = FontWeight.Bold)
            }
        }
    }
}
