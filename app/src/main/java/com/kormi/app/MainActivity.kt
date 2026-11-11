package com.kormi.app

import android.Manifest
import android.content.Context
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
            val context = LocalContext.current
            val sharedPref = context.getSharedPreferences("KormiPrefs", Context.MODE_PRIVATE)
            
            // 🌟 ফন্ট সাইজ লোড করা (ডিফল্ট ১.০)
            var currentFontScale by remember { mutableStateOf(sharedPref.getFloat("fontScale", 1.0f)) }

            KormiTheme(fontScale = currentFontScale) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val db = AppDatabase.getDatabase(context)
                    val scope = rememberCoroutineScope()
                    
                    val savedUserId = sharedPref.getString("userId", "") ?: ""
                    val savedRole = sharedPref.getString("role", "") ?: ""

                    var loggedInWorker by remember { mutableStateOf<Worker?>(null) }
                    var currentUserId by remember { mutableStateOf(savedUserId) }
                    var currentUserRole by remember { mutableStateOf(savedRole) }

                    LaunchedEffect(Unit) {
                        val adminWorker = db.workerDao().getWorkerByCard("ADMIN")
                        if (adminWorker == null) {
                            db.workerDao().insertWorker(
                                Worker(
                                    workerId = "ADMIN",
                                    fullName = "Admin",
                                    role = "Admin",
                                    designation = "Super Admin",
                                    companyId = 1,
                                    status = "Active",
                                    salary = 0.0
                                )
                            )
                        }

                        if (savedUserId.isNotEmpty() && savedRole != "ADMIN") {
                            loggedInWorker = db.workerDao().getWorkerByCard(savedUserId)
                        } else if (savedUserId == "ADMIN") {
                            loggedInWorker = db.workerDao().getWorkerByCard("ADMIN")
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

                    val startDestination = if (savedUserId.isNotEmpty()) {
                        if (savedRole == "Worker") "worker_dashboard" else "dashboard"
                    } else {
                        "login"
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") { LoginScreen(navController) { userId, role ->
                            currentUserId = userId
                            currentUserRole = role
                            if (role != "ADMIN") {
                                scope.launch { loggedInWorker = db.workerDao().getWorkerByCard(userId) }
                            } else {
                                scope.launch { loggedInWorker = db.workerDao().getWorkerByCard("ADMIN") }
                            }
                        } }
                        composable("register") { RegisterScreen(navController) }
                        composable("dashboard") { 
                            DashboardScreen(
                                navController = navController, 
                                role = currentUserRole, 
                                userName = if (currentUserRole == "ADMIN") "Admin" else (loggedInWorker?.fullName ?: "User"),
                                userId = currentUserId,
                                currentFontScale = currentFontScale,
                                onFontScaleChange = { newScale ->
                                    currentFontScale = newScale
                                    sharedPref.edit().putFloat("fontScale", newScale).apply()
                                }
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
                        composable("audit_logs") { AuditLogScreen(navController) }
                        composable("messages") { MessagesScreen(navController, currentUserId) }
                        composable("chat_detail/{senderId}/{receiverId}") { backStackEntry ->
                            ChatDetailScreen(navController, backStackEntry.arguments?.getString("senderId") ?: "", backStackEntry.arguments?.getString("receiverId") ?: "")
                        }
                        composable("call/{roomName}/{callType}") { backStackEntry ->
                            val roomName = backStackEntry.arguments?.getString("roomName") ?: ""
                            val callType = backStackEntry.arguments?.getString("callType") ?: "audio"
                            VideoCallScreen(navController, roomName, isVideoCall = callType == "video")
                        }
                        composable("create_group/{currentUserId}") { backStackEntry ->
                            CreateGroupScreen(navController, backStackEntry.arguments?.getString("currentUserId") ?: "")
                        }
                        composable("group_chat/{currentUserId}/{groupId}") { backStackEntry ->
                            GroupChatDetailScreen(navController, backStackEntry.arguments?.getString("currentUserId") ?: "", backStackEntry.arguments?.getString("groupId")?.toIntOrNull() ?: 0)
                        }
                        composable("notifications") { NotificationsScreen(navController, loggedInWorker, currentUserRole) }
                        composable("location") { LocationScreen(navController, loggedInWorker, currentUserRole) }
                    }
                }
            }
        }
    }
}
