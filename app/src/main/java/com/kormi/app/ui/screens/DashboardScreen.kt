package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy

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

@Composable
fun DashboardScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Navy, Navy.copy(alpha = 0.92f))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column {
                Text(
                    text = "Kormi",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Workforce Companion",
                    color = Gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Gold)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Dashboard",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
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

@Composable
private fun DashboardCard(item: DashboardItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Navy.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.titleEn,
                    tint = Navy,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = item.titleBn,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.titleEn,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gold,
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}
