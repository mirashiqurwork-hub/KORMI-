package com.kormi.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary

data class DashboardItem(val titleEn: String, val icon: ImageVector, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, role: String, userName: String) {
    var expandedMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var fontSizeLarge by remember { mutableStateOf(false) }

    val dashboardItems = listOf(
        DashboardItem("Workers", Icons.Outlined.People, "workers"),
        DashboardItem("Attendance", Icons.Outlined.CheckCircle, "attendance"),
        DashboardItem("Manpower", Icons.Outlined.Groups, "manpower"),
        DashboardItem("Production", Icons.Outlined.Build, "production"),
        DashboardItem("Messages", Icons.Outlined.Chat, "messages"),
        DashboardItem("Location", Icons.Outlined.LocationOn, "location"),
        DashboardItem("Breakdown", Icons.Outlined.Warning, "breakdown"),
        DashboardItem("Admin", Icons.Outlined.AdminPanelSettings, "admin")
    )

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
            
            // সোনালি ঢেউ (Golden Wave) ডিজাইন
            Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                val path = Path().apply {
                    moveTo(0f, 180f)
                    cubicTo(size.width * 0.3f, 230f, size.width * 0.7f, 130f, size.width, 180f)
                    lineTo(size.width, 0f)
                    lineTo(0f, 0f)
                    close()
                }
                drawPath(path, Navy)
                
                // সোনালি বর্ডার লাইন
                val goldPath = Path().apply {
                    moveTo(0f, 180f)
                    cubicTo(size.width * 0.3f, 230f, size.width * 0.7f, 130f, size.width, 180f)
                    lineTo(size.width, 184f)
                    cubicTo(size.width * 0.7f, 134f, size.width * 0.3f, 234f, 0f, 184f)
                    close()
                }
                drawPath(goldPath, Gold)
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
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
                            DropdownMenuItem(text = { Text("Edit Profile") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { expandedMenu = false })
                            DropdownMenuItem(
                                text = { Text(if (fontSizeLarge) "Normal Font" else "Large Font") }, 
                                leadingIcon = { Icon(Icons.Default.FormatSize, null) }, 
                                onClick = { fontSizeLarge = !fontSizeLarge; expandedMenu = false }
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
                                    navController.navigate("login") { popUpTo(0) { inclusive = true } } 
                                }
                            )
                        }
                    }
                }

                // Hero Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.TrendingUp, contentDescription = null, tint = Gold)
                            Spacer(Modifier.width(8.dp))
                            Text(text = "Workforce at a glance", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatBox("Present", "231", Color(0xFF4CAF50), Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            StatBox("Absent", "17", Color(0xFFF44336), Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            StatBox("Off Day", "12", Color(0xFFFFC107), Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Main Modules", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, bottom = 12.dp))

                // Grid Items
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

    // About Dialog (ফাউন্ডারের তথ্য)
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About", fontWeight = FontWeight.Bold, color = Navy) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // আপনার ছবি এখানে বসবে (পরে আমরা res/drawable থেকে ছবি লোড করব)
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
private fun StatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
private fun DashboardCard(item: DashboardItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = item.icon, contentDescription = item.titleEn, tint = Navy, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(text = item.titleEn, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Navy, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
