package com.kormi.app.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate // ক্যালকুলেটর আইকন ইমপোর্ট
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kormi.app.data.AppDatabase
import com.kormi.app.data.Production
import com.kormi.app.ui.theme.Gold
import com.kormi.app.ui.theme.Navy
import com.kormi.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val calendar = Calendar.getInstance()
    var selectedDateDb by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)) }
    var selectedDateDisplay by remember { mutableStateOf(SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(calendar.time)) }
    
    // ফর্ম স্টেট
    var unit by remember { mutableStateOf("UNIT-01") }
    var shift by remember { mutableStateOf("A") }
    var incharge by remember { mutableStateOf("") }
    
    // মেশিন ভ্যালু
    var cd by remember { mutableStateOf("") }
    var bd by remember { mutableStateOf("") }
    var fd by remember { mutableStateOf("") }
    var sf by remember { mutableStateOf("") }
    var rf by remember { mutableStateOf("") }
    var lc by remember { mutableStateOf("") }
    
    // প্যাকিং ভ্যালু
    var packBags by remember { mutableStateOf("") }
    var packKg by remember { mutableStateOf("") }

    var expandedUnit by remember { mutableStateOf(false) }
    var expandedShift by remember { mutableStateOf(false) }
    
    // সামারি ডায়ালগ স্টেট
    var showSummaryDialog by remember { mutableStateOf(false) }
    var summaryText by remember { mutableStateOf("") }

    val units = listOf("UNIT-01", "UNIT-02", "G/F", "F/F")
    val shifts = listOf("A", "B", "C", "P/A", "P/B", "P/C")

    val datePickerDialog = DatePickerDialog(
        context, { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDateDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            selectedDateDisplay = SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(cal.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Production Entry", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // ক্যালকুলেটর বাটন
                    IconButton(onClick = { navController.navigate("production_calculator") }) {
                        Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = Color.White)
                    }
                    // ডেট পিকার বাটন
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Date", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F4F7))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // হেডার ইনফো
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Date: $selectedDateDisplay", fontWeight = FontWeight.Bold, color = Navy)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Unit Dropdown
                        ExposedDropdownMenuBox(expanded = expandedUnit, onExpandedChange = { expandedUnit = it }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = unit, onValueChange = {}, readOnly = true, label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedUnit, onDismissRequest = { expandedUnit = false }) {
                                units.forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; expandedUnit = false }) }
                            }
                        }
                        // Shift Dropdown
                        ExposedDropdownMenuBox(expanded = expandedShift, onExpandedChange = { expandedShift = it }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = shift, onValueChange = {}, readOnly = true, label = { Text("Shift") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedShift) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedShift, onDismissRequest = { expandedShift = false }) {
                                shifts.forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { shift = s; expandedShift = false }) }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = incharge, onValueChange = { incharge = it }, label = { Text("Shift In-Charge Name") }, modifier = Modifier.fillMaxWidth())
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Machine Production (Kg)", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            // মেশিন এন্ট্রি কার্ড
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProdTextField("C/D", cd, Modifier.weight(1f)) { cd = it }
                        ProdTextField("B/D", bd, Modifier.weight(1f)) { bd = it }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProdTextField("F/D", fd, Modifier.weight(1f)) { fd = it }
                        ProdTextField("S/F", sf, Modifier.weight(1f)) { sf = it }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProdTextField("R/F", rf, Modifier.weight(1f)) { rf = it }
                        ProdTextField("L/C", lc, Modifier.weight(1f)) { lc = it }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Packing Section", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))

            // প্যাকিং কার্ড
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProdTextField("Bags (Count)", packBags, Modifier.weight(1f)) { packBags = it }
                        ProdTextField("Total Weight (Kg)", packKg, Modifier.weight(1f)) { packKg = it }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // সেভ বাটন
            Button(
                onClick = {
                    scope.launch {
                        val date = selectedDateDb
                        val u = unit
                        val s = shift
                        val inc = incharge

                        fun saveMachine(mName: String, kgStr: String) {
                            val kg = kgStr.toDoubleOrNull() ?: 0.0
                            if (kg > 0.0) {
                                scope.launch {
                                    db.productionDao().insertProduction(Production(date=date, unit=u, shift=s, section="MACHINE", machineName=mName, weightKg=kg, inchargeName=inc, reportedBy="Admin"))
                                }
                            }
                        }
                        saveMachine("C/D", cd)
                        saveMachine("B/D", bd)
                        saveMachine("F/D", fd)
                        saveMachine("S/F", sf)
                        saveMachine("R/F", rf)
                        saveMachine("L/C", lc)

                        val bBags = packBags.toIntOrNull() ?: 0
                        val bKg = packKg.toDoubleOrNull() ?: 0.0
                        if (bBags > 0 || bKg > 0.0) {
                            db.productionDao().insertProduction(Production(date=date, unit=u, shift=s, section="PACKING", machineName="PACKING", weightKg=bKg, packingBags=bBags, packingWeightKg=bKg, inchargeName=inc, reportedBy="Admin"))
                        }

                        summaryText = """
                            Unite :- $unit
                            Date:- $selectedDateDisplay
                            production summary :- 
                            C/D:- ${if(cd.isBlank()) "0" else cd} kg
                            B/D:- ${if(bd.isBlank()) "0" else bd} kg
                            F/D:- ${if(fd.isBlank()) "0" else fd} kg
                            S/F:- ${if(sf.isBlank()) "0" else sf} kg
                            R/F:- ${if(rf.isBlank()) "0" else rf} kg
                            L/C:- ${if(lc.isBlank()) "0" else lc} kg
                            Packing = ${if(packBags.isBlank()) "0" else packBags} Bag ${if(packKg.isBlank()) "0" else packKg} kg
                        """.trimIndent()
                        
                        showSummaryDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save & Generate Summary", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // সামারি ডায়ালগ
    if (showSummaryDialog) {
        AlertDialog(
            onDismissRequest = { showSummaryDialog = false },
            title = { Text("Production Summary", fontWeight = FontWeight.Bold, color = Navy) },
            text = { Text(text = summaryText, fontSize = 14.sp) },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, summaryText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) { Text("Share") }
                    
                    Button(
                        onClick = { 
                            showSummaryDialog = false 
                            navController.popBackStack() 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) { Text("Done") }
                }
            }
        )
    }
}

@Composable
fun ProdTextField(label: String, value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = modifier,
        singleLine = true
    )
}
