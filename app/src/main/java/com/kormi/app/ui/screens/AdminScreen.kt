package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin & Access", color = Color.White, fontWeight = FontWeight.Bold) },
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
            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AdminMenuCard(
                title = "Manage Companies",
                description = "View, edit or delete companies",
                icon = Icons.Default.Business
            ) {
                navController.navigate("manage_companies")
            }

            AdminMenuCard(
                title = "Access Control",
                description = "Manage who sees which module",
                icon = Icons.Default.Security
            ) {
                navController.navigate("access_control")
            }

            AdminMenuCard(
                title = "Add Company",
                description = "Register a new company",
                icon = Icons.Default.AddBusiness
            ) {
                navController.navigate("add_company")
            }

            AdminMenuCard(
                title = "Manage Passwords",
                description = "Change worker login passwords",
                icon = Icons.Default.Lock
            ) {
                navController.navigate("admin_passwords")
            }

            AdminMenuCard(
                title = "System Settings",
                description = "Configure app preferences",
                icon = Icons.Default.Settings
            ) {
                // ভবিষ্যতে এখানে সেটিংস আসবে
            }
        }
    }
}

@Composable
fun AdminMenuCard(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Navy.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = Navy)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Navy)
                Text(description, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}
