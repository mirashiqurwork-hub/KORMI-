package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary

data class DashboardItem(
    val titleBn: String,
    val titleEn: String,
    val icon: ImageVector,
    val route: String
)

private val dashboardItems = listOf(
    DashboardItem("কর্মী", "Workers", Icons.Outlined.People, "workers"),
    DashboardItem("উপস্থিতি", "Attendance", Icons.Outlined.CheckCircle, "attendance"),
    DashboardItem("জনবল", "Manpower", Icons.Outlined.Groups, "manpower"),
    DashboardItem("উৎপাদন", "Production", Icons.Outlined.Build, "production"),
    DashboardItem("বার্তা", "Messages", Icons.Outlined.Chat, "messages"),
    DashboardItem("অবস্থান", "Location", Icons.Outlined.LocationOn, "location"),
    DashboardItem("বিজ্ঞপ্তি", "Notifications", Icons.Outlined.Notifications, "notifications"),
    DashboardItem("অ্যাডমিন", "Admin & Access", Icons.Outlined.AdminPanelSettings, "admin")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Gold,
                        selectedTextColor = Gold,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = Gold.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.People, contentDescription = "Workers") },
                    label = { Text("Workers", fontSize = 10.sp) },
                    selected = false,
                    onClick = { navController.navigate("workers") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = "Attendance") },
                    label = { Text("Attendance", fontSize = 10.sp) },
                    selected = false,
                    onClick = { navController.navigate("attendance") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Chat, contentDescription = "Messages") },
                    label = { Text("Messages", fontSize = 10.sp) },
                    selected = false,
                    onClick = { navController.navigate("messages") },
                    colors = NavigationBarItemDefaults.colors(unselectedIconColor = TextSecondary)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F4F7)) // Very light grey/blue background
                .padding(paddingValues)
        ) {
            // Top Header (Profile & Welcome)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Welcome back",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Dashboard",
                        color = Navy,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Profile Box
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Navy.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Navy,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Hero Card (Workforce at a glance)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Navy),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "KORMI",
                        color = Gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Workforce at a glance",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatBox("Workers", "0", Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        StatBox("Present", "0", Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        StatBox("Pending", "0", Modifier.weight(1f))
                    }
                }
            }

            // Section Title
            Text(
                text = "Main Modules",
                color = Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
            )

            // Grid Items
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(dashboardItems) { item ->
                    DashboardCard(item) {
                        navController.navigate(item.route)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun DashboardCard(item: DashboardItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0F4F8)), // Light blue-ish grey icon background
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.titleEn,
                    tint = Navy,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = item.titleEn,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.titleBn,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
        }
    }
}
