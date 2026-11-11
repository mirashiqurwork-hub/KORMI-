package com.kormi.app.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DashboardItem(val titleEn: String, val icon: ImageVector, val route: String, val allowedRoles: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController, 
    role: String, 
    userName: String, 
    userId: String,
    currentFontScale: Float,
    onFontScaleChange: (Float) -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var expandedMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }

    var presentCount by remember { mutableStateOf("0") }
    var absentCount by remember { mutableStateOf("0") }
    var offDayCount by remember { mutableStateOf("0") }

    var userAllowedModules by remember { mutableStateOf("") }
    LaunchedEffect(userId) {
        if (userId != "ADMIN" && userId.isNotEmpty()) {
            val worker = db.workerDao().getWorkerByCard(userId)
            userAllowedModules = worker?.allowedModules ?: ""
        } else {
            userAllowedModules = "ALL"
        }
    }

    val allItems = listOf(
        DashboardItem("Active", Icons.Outlined.CheckCircle, "worker_hierarchy/Active", listOf("ADMIN", "Management", "Officer")),
        DashboardItem("Inactive", Icons.Outlined.Cancel, "worker_hierarchy/Inactive", listOf("ADMIN", "Management")),
        DashboardItem("Workers", Icons.Outlined.People, "workers", listOf("ADMIN", "Officer", "Management")),
        DashboardItem("Attendance", Icons.Outlined.CheckCircle, "attendance", listOf("ADMIN", "Officer", "Management")),
        DashboardItem("Manpower", Icons.Outlined.Groups, "manpower", listOf("ADMIN", "Officer", "Management")),
        DashboardItem("Production", Icons.Outlined.Build, "production", listOf("ADMIN", "Officer", "Management")),
        DashboardItem("Breakdown", Icons.Outlined.Warning, "breakdown", listOf("ADMIN", "Officer", "Management")),
        DashboardItem("Messages", Icons.Outlined.Chat, "messages", listOf("ADMIN", "Officer", "Management", "Worker")),
        DashboardItem("Location", Icons.Outlined.LocationOn, "location", listOf("ADMIN", "Officer", "Management")),
        DashboardItem("Admin", Icons.Outlined.AdminPanelSettings, "admin", listOf("ADMIN"))
    )
    
    val dashboardItems = allItems.filter { item ->
        val roleMatch = item.allowedRoles.contains(role)
        val permissionMatch = userAllowedModules == "ALL" || userAllowedModules.contains(item.titleEn)
        roleMatch && permissionMatch
    }

    LaunchedEffect(Unit) {
        scope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val workers = db.workerDao().getAllActiveWorkersList()
            val attendanceList = db.attendanceDao().getAttendanceByDate(today)
            val attendanceMap = attendanceList.associate { it.workerId to it.status }

            var present = 0
            var absent = 0
            var offDay = 0

            workers.forEach { worker ->
                when (attendanceMap[worker.workerId]) {
                    "Present" -> present++
                    "Absent" -> absent++
                    "Off Day" -> offDay++
                }
            }
            presentCount = present.toString()
            absentCount = absent.toString()
            offDayCount = offDay.toString()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Dashboard") },
                    label = { Text("Home", fontSize = 10.sp) },
                    selected = true, onClick = { },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Gold, selectedTextColor = Gold, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = Gold.copy(alpha = 0.15f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Chat, contentDescription = "Chat") },
                    label = { Text("Chat", fontSize = 10.sp) },
                    selected = false, onClick = { navController.navigate("messages") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Notifications, contentDescription = "Alerts") },
                    label = { Text("Alerts", fontSize = 10.sp) },
                    selected = false, onClick = { navController.navigate("notifications") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Admin") },
                    label = { Text("Admin", fontSize = 10.sp) },
                    selected = false, onClick = { navController.navigate("admin") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary)
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding)) {
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val startX = w * 1.1f
                val startY = h * 0.2f
                val endX = -w * 0.2f
                val endY = h * 0.6f
                
                val steps = 8
                for (i in 0 until steps) {
                    val fraction = i.toFloat() / steps
                    val x = startX + (endX - startX) * fraction
                    val y = startY + (endY - startY) * fraction
                    val scale = 1f - (fraction * 0.7f)
                    
                    val frameWidth = 120f * scale
                    val frameHeight = 400f * scale
                    
                    drawRoundRect(
                        color = Color(0xFFE5E9F0).copy(alpha = 0.6f),
                        topLeft = Offset(x, y),
                        size = Size(frameWidth, frameHeight),
                        cornerRadius = CornerRadius(20f, 20f)
                    )
                    
                    for (j in 1..4) {
                        drawCircle(
                            color = Color(0xFFD0D7E5),
                            radius = 10f * scale,
                            center = Offset(x + (frameWidth * 0.2f), y + (frameHeight * (j / 5f)))
                        )
                    }
                }
            }

            Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                val path = Path().apply {
                    moveTo(0f, 180f)
                    cubicTo(size.width * 0.3f, 230f, size.width * 0.7f, 130f, size.width, 180f)
                    lineTo(size.width, 0f)
                    lineTo(0f, 0f)
                    close()
                }
                drawPath(path, Navy)
                
                val goldPath = Path().apply {
                    moveTo(0f, 180f)
                    cubicTo(size.width * 0.3f, 230f, size.width * 0.7f, 130f, size.width, 180f)
                    lineTo(size.width, 184f)
                    cubicTo(size.width * 0.7f, 134f, size.width * 0.3f, 234f, 0f, 184f)
                    close()
                }
                drawPath(goldPath, Gold)
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, h - 80f)
                    lineTo(w * 0.15f, h - 120f)
                    lineTo(w * 0.15f, h - 200f)
                    lineTo(w * 0.2f, h - 200f)
                    lineTo(w * 0.2f, h - 150f)
                    lineTo(w * 0.35f, h - 150f)
                    lineTo(w * 0.45f, h - 220f)
                    lineTo(w * 0.55f, h - 150f)
                    lineTo(w * 0.7f, h - 150f)
                    lineTo(w * 0.7f, h - 260f)
                    lineTo(w * 0.75f, h - 260f)
                    lineTo(w * 0.75f, h - 130f)
                    lineTo(w * 0.9f, h - 130f)
                    lineTo(w * 0.95f, h - 180f)
                    lineTo(w * 1f, h - 100f)
                    lineTo(w * 1f, h)
                    close()
                }
                drawPath(path, Color(0xFFE8EDF2).copy(alpha = 0.8f))

                drawCircle(Color(0xFFEDF1F5).copy(alpha = 0.8f), radius = 30f, center = Offset(w * 0.17f, h - 250f))
                drawCircle(Color(0xFFEDF1F5).copy(alpha = 0.8f), radius = 50f, center = Offset(w * 0.21f, h - 310f))
                drawCircle(Color(0xFFEDF1F5).copy(alpha = 0.8f), radius = 40f, center = Offset(w * 0.72f, h - 310f))
                drawCircle(Color(0xFFEDF1F5).copy(alpha = 0.8f), radius = 60f, center = Offset(w * 0.75f, h - 370f))
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Welcome back,", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text(text = userName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Box {
                        IconButton(onClick = { expandedMenu = true }) {
                            Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                            }
                        }
                        DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Edit Profile") }, 
                                leadingIcon = { Icon(Icons.Default.Edit, null) }, 
                                onClick = { 
                                    expandedMenu = false
                                    navController.navigate("worker_details/$userId") 
                                }
                            )
                            
                            if (role != "Worker") {
                                DropdownMenuItem(
                                    text = { Text("Manage Passwords") }, 
                                    leadingIcon = { Icon(Icons.Default.Lock, null) }, 
                                    onClick = { 
                                        expandedMenu = false
                                        navController.navigate("admin_passwords") 
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("Font Size") }, 
                                leadingIcon = { Icon(Icons.Default.FormatSize, null) }, 
                                onClick = { 
                                    showFontDialog = true
                                    expandedMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("About") }, 
                                leadingIcon = { Icon(Icons.Default.Info, null) }, 
                                onClick = { showAboutDialog = true; expandedMenu = false }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Logout", color = Color.Red) }, 
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red) }, 
                                onClick = { 
                                    expandedMenu = false
                                    context.getSharedPreferences("KormiPrefs", Context.MODE_PRIVATE).edit().clear().apply()
                                    navController.navigate("login") { popUpTo(0) { inclusive = true } } 
                                }
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy.copy(alpha = 0.95f)),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.TrendingUp, contentDescription = null, tint = Gold)
                            Spacer(Modifier.width(8.dp))
                            Text(text = "Workforce at a glance (Today)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatBox("Present", presentCount, Color(0xFF4CAF50), Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            StatBox("Absent", absentCount, Color(0xFFF44336), Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            StatBox("Off Day", offDayCount, Color(0xFFFFC107), Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Main Modules", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, bottom = 12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(dashboardItems) { item ->
                        DashboardCard(item) { navController.navigate(item.route) }
                    }
                }
            }
        }
    }

    if (showFontDialog) {
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = { Text("Select Font Size", fontWeight = FontWeight.Bold, color = Navy) },
            text = {
                Column {
                    FontSizeOption("Small", 0.8f, currentFontScale, onFontScaleChange, context) { showFontDialog = false }
                    FontSizeOption("Normal", 1.0f, currentFontScale, onFontScaleChange, context) { showFontDialog = false }
                    FontSizeOption("Large", 1.2f, currentFontScale, onFontScaleChange, context) { showFontDialog = false }
                    FontSizeOption("Extra Large", 1.4f, currentFontScale, onFontScaleChange, context) { showFontDialog = false }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontDialog = false }) {
                    Text("Close", color = Navy, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About", fontWeight = FontWeight.Bold, color = Navy) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = "CEO", modifier = Modifier.size(60.dp), tint = Color.Gray)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("MIR ASHIQUR RAHMAN", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Navy)
                    Text("Founder and CEO", fontSize = 14.sp, color = Gold)
                    Spacer(Modifier.height(4.dp))
                    Text("\"KORMI\" App and software.", fontSize = 12.sp, color = TextSecondary)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = Navy, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun FontSizeOption(
    label: String, 
    scale: Float, 
    currentScale: Float, 
    onScaleChange: (Float) -> Unit, 
    context: Context,
    onDismiss: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable { 
            onScaleChange(scale)
            android.widget.Toast.makeText(context, "ফন্ট সাইজ: $label", android.widget.Toast.LENGTH_SHORT).show()
            onDismiss()
        }.padding(vertical = 8.dp)
    ) {
        RadioButton(
            selected = currentScale == scale,
            onClick = null
        )
        Spacer(Modifier.width(8.dp))
        Text(text = label, fontSize = (14 * scale).sp)
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.15f)).padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
private fun DashboardCard(item: DashboardItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.1f))
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.titleEn,
                tint = Navy,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.titleEn,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Navy,
                textAlign = TextAlign.Center
            )
        }
    }
}
