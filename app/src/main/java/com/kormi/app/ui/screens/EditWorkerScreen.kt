package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkerScreen(navController: NavController, workerId: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var worker by remember { mutableStateOf<Worker?>(null) }

    LaunchedEffect(workerId) {
        worker = db.workerDao().getWorkerByCard(workerId)
    }

    if (worker == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Navy)
        }
        return
    }

    var fullName by remember { mutableStateOf(worker!!.fullName) }
    var phone by remember { mutableStateOf(worker!!.phone) }
    var unit by remember { mutableStateOf(worker!!.unit ?: "") }
    var section by remember { mutableStateOf(worker!!.section ?: "") }
    var shift by remember { mutableStateOf(worker!!.shift ?: "") }
    var offDay by remember { mutableStateOf(worker!!.weeklyOffDay ?: "") }
    var address by remember { mutableStateOf(worker!!.presentAddress ?: "") }
    var salary by remember { mutableStateOf(worker!!.salary.toString()) }
    var role by remember { mutableStateOf(worker!!.role) }
    var designation by remember { mutableStateOf(worker!!.designation) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Information", color = Color.White, fontWeight = FontWeight.Bold) },
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
            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // এখানে WorkerTextField এর বদলে সরাসরি OutlinedTextField ব্যবহার করা হয়েছে
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = section, onValueChange = { section = it }, label = { Text("Section") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = shift, onValueChange = { shift = it }, label = { Text("Shift") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = offDay, onValueChange = { offDay = it }, label = { Text("Weekly Off Day") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Salary") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            
            OutlinedTextField(value = role, onValueChange = { }, label = { Text("Role (Read Only)") }, readOnly = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
            OutlinedTextField(value = designation, onValueChange = { }, label = { Text("Designation (Read Only)") }, readOnly = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        val updatedWorker = worker!!.copy(
                            fullName = fullName,
                            phone = phone,
                            unit = unit,
                            section = section,
                            shift = shift,
                            weeklyOffDay = offDay,
                            presentAddress = address,
                            salary = salary.toDoubleOrNull() ?: 0.0
                        )
                        db.workerDao().updateWorker(updatedWorker)
                        android.widget.Toast.makeText(context, "সফলভাবে আপডেট হয়েছে!", android.widget.Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update Information", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
