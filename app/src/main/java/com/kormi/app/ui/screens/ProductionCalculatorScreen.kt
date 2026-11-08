package com.kormi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionCalculatorScreen(navController: NavController) {
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
            // ট্যাব সিলেকশন
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

    CalcInput("Speed (m/min)", speed) { speed = it }
    CalcInput("Time (Hours)", time) { time = it }
    CalcInput("Efficiency (%)", efficiency) { efficiency = it }
    CalcInput("Sliver Hank", sliverHank) { sliverHank = it }

    Button(
        onClick = {
            val s = speed.toDoubleOrNull() ?: 0.0
            val t = time.toDoubleOrNull() ?: 0.0
            val e = efficiency.toDoubleOrNull() ?: 0.0
            val h = sliverHank.toDoubleOrNull() ?: 0.0
            
            if (h > 0) {
                result = (s * 1.0936 * 60 * t * e) / (840 * h * 2.2046 * 100)
            } else { result = 0.0 }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Navy)
    ) { Text("Calculate") }

    CalcResult(result)
}

@Composable
fun SimplexCalculator() {
    var speed by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf("") }
    var spindle by remember { mutableStateOf("") }
    var tpi by remember { mutableStateOf("") }
    var rovingHank by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }

    CalcInput("Speed (Rpm)", speed) { speed = it }
    CalcInput("Time (Hours)", time) { time = it }
    CalcInput("Efficiency (%)", efficiency) { efficiency = it }
    CalcInput("No of Spindle", spindle) { spindle = it }
    CalcInput("TPI", tpi) { tpi = it }
    CalcInput("Roving Hank", rovingHank) { rovingHank = it }

    Button(
        onClick = {
            val s = speed.toDoubleOrNull() ?: 0.0
            val t = time.toDoubleOrNull() ?: 0.0
            val e = efficiency.toDoubleOrNull() ?: 0.0
            val sp = spindle.toDoubleOrNull() ?: 0.0
            val tp = tpi.toDoubleOrNull() ?: 0.0
            val rh = rovingHank.toDoubleOrNull() ?: 0.0
            
            if (tp > 0 && rh > 0) {
                result = (s * 60 * t * sp * e) / (tp * 36 * 840 * rh * 2.2046 * 100)
            } else { result = 0.0 }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Navy)
    ) { Text("Calculate") }

    CalcResult(result)
}

@Composable
fun RingCalculator() {
    var speed by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf("") }
    var spindle by remember { mutableStateOf("") }
    var tpi by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }

    CalcInput("Speed (Rpm)", speed) { speed = it }
    CalcInput("Time (Hours)", time) { time = it }
    CalcInput("Efficiency (%)", efficiency) { efficiency = it }
    CalcInput("No of Spindle", spindle) { spindle = it }
    CalcInput("TPI", tpi) { tpi = it }
    CalcInput("Count", count) { count = it }

    Button(
        onClick = {
            val s = speed.toDoubleOrNull() ?: 0.0
            val t = time.toDoubleOrNull() ?: 0.0
            val e = efficiency.toDoubleOrNull() ?: 0.0
            val sp = spindle.toDoubleOrNull() ?: 0.0
            val tp = tpi.toDoubleOrNull() ?: 0.0
            val c = count.toDoubleOrNull() ?: 0.0
            
            if (tp > 0 && c > 0) {
                result = (s * 60 * t * sp * e) / (tp * 36 * 840 * c * 2.2046 * 100)
            } else { result = 0.0 }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Navy)
    ) { Text("Calculate") }

    CalcResult(result)
}

@Composable
fun WindingCalculator() {
    var speed by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf("") }
    var drums by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }

    CalcInput("Speed (m/min)", speed) { speed = it }
    CalcInput("Time (Hours)", time) { time = it }
    CalcInput("Efficiency (%)", efficiency) { efficiency = it }
    CalcInput("No of Drums", drums) { drums = it }
    CalcInput("Count", count) { count = it }

    Button(
        onClick = {
            val s = speed.toDoubleOrNull() ?: 0.0
            val t = time.toDoubleOrNull() ?: 0.0
            val e = efficiency.toDoubleOrNull() ?: 0.0
            val d = drums.toDoubleOrNull() ?: 0.0
            val c = count.toDoubleOrNull() ?: 0.0
            
            if (c > 0) {
                result = (s * 1.0936 * 60 * t * d * e) / (840 * c * 2.2046 * 100)
            } else { result = 0.0 }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Navy)
    ) { Text("Calculate") }

    CalcResult(result)
}

@Composable
fun CalcInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
    )
}

@Composable
fun CalcResult(result: Double?) {
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
                Text("Result (Kg/h)", fontSize = 14.sp, color = Navy)
                Text(
                    text = String.format(Locale.US, "%.2f", result),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )
            }
        }
    }
}
