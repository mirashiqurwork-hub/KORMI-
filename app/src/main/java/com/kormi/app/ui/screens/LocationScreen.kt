package com.kormi.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Worker
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(navController: NavController, loggedInWorker: Worker?, currentUserRole: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "লোকেশন পারমিশন ছাড়া এটি কাজ করবে না!", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateLocation() {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) 
                ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (location != null) {
                scope.launch {
                    loggedInWorker?.let { worker ->
                        val updatedWorker = worker.copy(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        db.workerDao().updateWorker(updatedWorker)
                        Toast.makeText(context, "লোকেশন সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "লোকেশন পাওয়া যায়নি। জিপিএস চালু করুন।", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "পারমিশন সমস্যা হয়েছে!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Location", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        if (!hasPermission) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("লোকেশন চালু করতে পারমিশন দিন", fontWeight = FontWeight.Bold, color = Navy)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) {
                        Text("Allow Location")
                    }
                }
            }
        } else {
            if (currentUserRole == "Worker") {
                Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("আপনার বর্তমান লোকেশন আপডেট করুন", fontWeight = FontWeight.Bold, color = Navy, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("লোকেশন: ${loggedInWorker?.latitude ?: "Not set"}, ${loggedInWorker?.longitude ?: "Not set"}", color = TextSecondary)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { updateLocation() },
                            colors = ButtonDefaults.buttonColors(containerColor = Navy)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Update My Location")
                        }
                    }
                }
            } else {
                val allWorkers by db.workerDao().getAllWorkers().collectAsState(initial = emptyList())
                val workersWithLocation = allWorkers.filter { it.latitude != null && it.longitude != null }

                if (workersWithLocation.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("কোনো কর্মীর লোকেশন পাওয়া যায়নি।", color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().background(Color(0xFFF2F4F7)).padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(workersWithLocation) { worker ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(worker.fullName, fontWeight = FontWeight.Bold, color = Navy)
                                        Text("Card: ${worker.workerId} | Unit: ${worker.unit}", fontSize = 12.sp, color = TextSecondary)
                                    }
                                    IconButton(
                                        onClick = {
                                            val uri = Uri.parse("geo:${worker.latitude},${worker.longitude}?q=${worker.latitude},${worker.longitude}(${Uri.encode(worker.fullName)})")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            intent.setPackage("com.google.android.apps.maps")
                                            if (intent.resolveActivity(context.packageManager) != null) {
                                                context.startActivity(intent)
                                            } else {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = "Map", tint = Gold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
