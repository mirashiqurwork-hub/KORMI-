package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulePlaceholderScreen(
    titleBn: String,
    titleEn: String,
    navController: NavController,
    description: String = "এই মডিউলটি পরবর্তী ধাপে সম্পূর্ণ কার্যকর হবে।"
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(text = titleBn, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = titleEn, fontSize = 11.sp, color = Gold, fontWeight = FontWeight.Medium)
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(88.dp).background(Navy.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Construction, contentDescription = null, tint = Navy, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(text = titleBn, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Navy)
            Spacer(Modifier.height(8.dp))
            Text(text = description, fontSize = 14.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
