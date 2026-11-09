package com.kormi.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Worker
import com.kormi.app.ui.screens.*
import com.kormi.app.ui.theme.KormiTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KormiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val context = LocalContext.current
                    val db = AppDatabase.getDatabase(context)
                    val scope = rememberCoroutineScope()
                    
                    var loggedInWorker by remember { mutableStateOf<Worker?>(null) }
                    var currentUserId by remember { mutableStateOf("") }
                    var currentUserRole by remember { mutableStateOf("") }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted -> }

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }

                        val companies = db.companyDao().getAllCompaniesSync()
                        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val todayDayName = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date()).uppercase()

                        companies.forEach { comp ->
                            if (comp.shiftChangeDay.equals(todayDayName, ignoreCase = true) && comp.lastRotationDate != todayDate) {
                                db.workerDao().rotateShifts(comp.companyId)
                                db.companyDao().insertCompany(comp.copy(lastRotationDate = todayDate))
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") { 
                            LoginScreen(navController) { userId, role ->
                                currentUserId = userId
                                currentUserRole = role
                                if (role != "ADMIN") {
                                    scope.launch { loggedInWorker = db.workerDao().getWorkerByCard(userId) }
                                }
                            } 
                        }
                        composable("register") { RegisterScreen(navController) }
                        
                        composable("dashboard") { 
                            DashboardScreen(
                                navController = navController, 
                                role = currentUserRole, 
                                userName = if (currentUserRole == "ADMIN") "Admin" else (loggedInWorker?.fullName ?: "User"),
                                userId = currentUserId
                            ) 
                        }
                        
                        composable("worker_hierarchy/{status}") { backStackEntry ->
                            val status = backStackEntry.arguments?.getString("status") ?: "Active"
                            WorkerHierarchyScreen(navController, status, loggedInWorker, currentUserRole)
                        }
                        
                        composable("worker_dashboard") { WorkerDashboardScreen(navController, loggedInWorker) }
                        
                        composable("worker_details/{workerId}") { backStackEntry ->
                            val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
                            var selectedWorker by remember { mutableStateOf<Worker?>(null) }
                            LaunchedEffect(workerId) { selectedWorker = db.workerDao().getWorkerByCard(workerId) }
                            WorkerProfileScreen(navController, selectedWorker, currentUserRole)
                        }
                        
                        composable("edit_worker/{workerId}") { backStackEntry ->
                            val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
                            EditWorkerScreen(navController, workerId)
                        }
                        
                        composable("workers") { WorkersScreen(navController, loggedInWorker, currentUserRole) }
                        composable("add_worker") { AddWorkerScreen(navController) }
                        
                        composable("ocr_worker") { OcrScreen(navController, "Worker List", loggedInWorker, currentUserRole) }
                        composable("ocr_attendance") { OcrAttendanceScreen(navController, loggedInWorker) }
                        
                        composable("attendance") { AttendanceScreen(navController, loggedInWorker) }
                        composable("overtime_entry") { OvertimeEntryScreen(navController) }
                        
                        composable("manpower") { ManpowerScreen(navController) }
                        
                        composable("production") { ProductionScreen(navController) }
                        composable("production_ocr") { ProductionOcrScreen(navController, loggedInWorker) }
                        composable("production_calculator") { ProductionCalculatorScreen(navController) }
                        composable("production_history") { ProductionHistoryScreen(navController) }
                        
                        composable("breakdown") { BreakdownScreen(navController) }
                        composable("admin") { AdminScreen(navController) }
                        
                        composable("admin_passwords") { ManagePasswordsScreen(navController, loggedInWorker) }
                        composable("access_control") { AccessControlScreen(navController) }
                        
                        composable("add_company") { AddCompanyScreen(navController) }
                        composable("manage_companies") { ManageCompaniesScreen(navController) }
                        
                        composable("messages") { MessagesScreen(navController, currentUserId) }
                        composable("chat_detail/{senderId}/{receiverId}") { backStackEntry ->
                            val senderId = backStackEntry.arguments?.getString("senderId") ?: ""
                            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
                            ChatDetailScreen(navController, senderId, receiverId)
                        }
                        
                        composable("notifications") { 
                            NotificationsScreen(navController, loggedInWorker, currentUserRole) 
                        }
                        
                        composable("location") { 
                            LocationScreen(navController, loggedInWorker, currentUserRole) 
                        }
                    }
                }
            }
        }
    }
}
