package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionScreen(navController: NavController) {
    var selectedProcess by remember { mutableStateOf(0) }
    val processes = listOf("Drawing", "Simplex", "Ring", "Winding")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Production Calculator", color = Color.White, fontWeight = FontWeight.Bold) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF2F4F7))
        ) {
            // Tabs for Processes
            ScrollableTabRow(
                selectedTabIndex = selectedProcess,
                containerColor = Navy,
                contentColor = Color.White,
                edgePadding = 0.dp
            ) {
                processes.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedProcess == index,
                        onClick = { selectedProcess = index },
                        text = { Text(title, color = if (selectedProcess == index) Gold else Color.White.copy(alpha = 0.7f)) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedProcess) {
                    0 -> DrawingCalculator()
                    1 -> SimplexCalculator()
                    2 -> RingCalculator()
                    3 -> WindingCalculator()
                }
            }
        }
    }
}

@Composable
fun DrawingCalculator() {
    var speed by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf("") }
    var sliverHank by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }

    NumberInput("Speed (m/min)", speed) { speed = it }
    NumberInput("Time (Hours)", time) { time = it }
    NumberInput("Efficiency (%)", efficiency) { efficiency = it }
    NumberInput("Sliver Hank", sliverHank) { sliverHank = it }

    Button(
        onClick = {
            val s = speed.toDoubleOrNull() ?: 0.0
            val t = time.toDoubleOrNull() ?: 0.0
            val e = efficiency.toDoubleOrNull() ?: 0.0
            val h = sliverHank.toDoubleOrNull() ?: 0.0
            
            if (h > 0) {
                // Drawing (Kg/h) = (Speed(m/min) * 1.0936 * 60 * Time (Hours) * Eff%) / (840 * Sliver Hank * 2.2046 * 100)
                result = (s * 1.0936 * 60 * t * e) / (840 * h * 2.2046 * 100)
            } else { result = 0.0 }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Navy)
    ) {
        Text("Calculate", fontSize = 16.sp)
    }

    ResultDisplay(result, "Kg/h")
}

@Composable
fun SimplexCalculator() {
    var speed by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf("") }
    var noOfSpindle by remember { mutableStateOf("") }
    var tpi by remember { mutableStateOf("") }
    var rovingHank by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }

    NumberInput("Speed (Rpm)", speed) { speed = it }
    NumberInput("Time (Hours)", time) { time = it }
    NumberInput("Efficiency (%)", efficiency) { efficiency = it }
    NumberInput("No of Spindle", noOfSpindle) { noOfSpindle = it }
    NumberInput("TPI", tpi) { tpi = it }
    NumberInput("Roving Hank", rovingHank) { rovingHank = it }

    Button(
        onClick = {
            val s = speed.toDoubleOrNull() ?: 0.0
            val t = time.toDoubleOrNull() ?: 0.0
            val e = efficiency.toDoubleOrNull() ?: 0.0
            val n = noOfSpindle.toDoubleOrNull() ?: 0.0
            val tp = tpi.toDoubleOrNull() ?: 0.0
            val rh = rovingHank.toDoubleOrNull() ?: 0.0
            
            if (tp > 0 && rh > 0) {
                // Simplex (Kg/h) = (Speed(Rpm) * 60 * Time (Hours) * No of Spindle * Eff%) / (TPI * 36 * 840 * Roving Hank * 2.2046 * 100)
                result = (s * 60 * t * n * e) / (tp * 36 * 840 * rh * 2.2046 * 100)
            } else { result = 0.0 }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Navy)
    ) {
        Text("Calculate", fontSize = 16.sp)
    }

    ResultDisplay(result, "Kg/h")
}

@Composable
fun RingCalculator() {
    var speed by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf("") }
    var noOfSpindle by remember { mutableStateOf("") }
    var tpi by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }

    NumberInput("Speed (Rpm)", speed) { speed = it }
    NumberInput("Time (Hours)", time) { time = it }
    NumberInput("Efficiency (%)", efficiency) { efficiency = it }
    NumberInput("No of Spindle", noOfSpindle) { noOfSpindle = it }
    NumberInput("TPI", tpi) { tpi = it }
    NumberInput("Count", count) { count = it }

    Button(
        onClick = {
            val s = speed.toDoubleOrNull() ?: 0.0
            val t = time.toDoubleOrNull() ?: 0.0
            val e = efficiency.toDoubleOrNull() ?: 0.0
            val n = noOfSpindle.toDoubleOrNull() ?: 0.0
            val tp = tpi.toDoubleOrNull() ?: 0.0
            val c = count.toDoubleOrNull() ?: 0.0
            
            if (tp > 0 && c > 0) {
                // Ring (Kg/h) = (Speed(Rpm) * 60 * Time (Hours) * No of Spindle * Eff%) / (TPI * 36 * 840 * Count * 2.2046 * 100)
                result = (s * 60 * t * n * e) / (tp * 36 * 840 * c * 2.2046 * 100)
            } else { result = 0.0 }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Navy)
    ) {
        Text("Calculate", fontSize = 16.sp)
    }

    ResultDisplay(result, "Kg/h")
}

@Composable
fun WindingCalculator() {
    var speed by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf("") }
    var noOfDrums by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }

    NumberInput("Speed (m/min)", speed) { speed = it }
    NumberInput("Time (Hours)", time) { time = it }
    NumberInput("Efficiency (%)", efficiency) { efficiency = it }
    NumberInput("No of Drums", noOfDrums) { noOfDrums = it }
    NumberInput("Count", count) { count = it }

    Button(
        onClick = {
            val s = speed.toDoubleOrNull() ?: 0.0
            val t = time.toDoubleOrNull() ?: 0.0
            val e = efficiency.toDoubleOrNull() ?: 0.0
            val n = noOfDrums.toDoubleOrNull() ?: 0.0
            val c = count.toDoubleOrNull() ?: 0.0
            
            if (c > 0) {
                // Winding (Kg/h) = (Speed(m/min) * 1.0936 * 60 * Time (Hours) * No Of Drum * Eff%) / (840 * Count * 2.2046 * 100)
                result = (s * 1.0936 * 60 * t * n * e) / (840 * c * 2.2046 * 100)
            } else { result = 0.0 }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Navy)
    ) {
        Text("Calculate", fontSize = 16.sp)
    }

    ResultDisplay(result, "Kg/h")
}

@Composable
fun NumberInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
fun ResultDisplay(result: Double?, unit: String) {
    if (result != null) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Navy.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Result", fontSize = 14.sp, color = Navy)
                Text(
                    text = String.format(Locale.US, "%.2f $unit", result),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )
            }
        }
    }
}
