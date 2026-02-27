package com.example.mymess.ui_for_admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymess.data.StudentRepository
import com.example.mymess.utils.CsvExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: androidx.navigation.NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Time-based Dynamic Button State
    var currentMealType by remember { mutableStateOf(getMealType()) }
    
    // Update time every minute to ensure button stays correct
    LaunchedEffect(Unit) {
        while(true) {
            currentMealType = getMealType()
            delay(60000) // Check every minute
        }
    }

    var studentNumberInput by remember { mutableStateOf("") }
    
    // Menu Strings (simplified state for now)
    // Menu Strings (Persistent)
    val breakfastMenu = StudentRepository.breakfastMenu
    val lunchMenu = StudentRepository.lunchMenu
    val dinnerMenu = StudentRepository.dinnerMenu
    
    // Dialog
    var showEditDialog by remember { mutableStateOf(false) }
    var editingMealType by remember { mutableStateOf("") }
    var editingMenuValue by remember { mutableStateOf("") }

    // Logic to mark attendance using Repository
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    // Renew Dialog State
    var showRenewDialogForStudent by remember { mutableStateOf<com.example.mymess.data.Student?>(null) }

    fun markAttendance() {
        if (studentNumberInput.isNotEmpty()) {
            val inputId = studentNumberInput.trim()
            // Find student by EXACT ID first
            val student = StudentRepository.getStudent(inputId)
            
            if (student != null) {
                val result = StudentRepository.markAttendance(student.id, currentMealType)
                scope.launch {
                    if (result.isSuccess) {
                        // UX: Hide Keyboard
                        keyboardController?.hide()
                        // UX: Show Name in Snackbar
                        snackbarHostState.showSnackbar("Marked ${currentMealType} for ${student.name}")
                        studentNumberInput = "" // Clear
                    } else {
                         val errorMsg = result.exceptionOrNull()?.message ?: ""
                         if (errorMsg.contains("credits left")) {
                             // Prompt to Renew
                             showRenewDialogForStudent = student
                         } else {
                             snackbarHostState.showSnackbar("Error: $errorMsg")
                         }
                    }
                }
            } else {
                 scope.launch { snackbarHostState.showSnackbar("Student with ID $inputId not found!") }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Welcome Kuldeep",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 4.dp).fillMaxWidth(),

                        )
                        Text(
                            "Manage your mess efficiently",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF8F1),
                    titleContentColor = Color(0xFF1E293B)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF8F1))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                 // 1. Menu Card
                MenuCard(
                    breakfast = breakfastMenu,
                    lunch = lunchMenu,
                    dinner = dinnerMenu,
                    onEditClick = { 
                        editingMealType = it
                        editingMenuValue = when(it) {
                            "Breakfast" -> breakfastMenu
                            "Lunch" -> lunchMenu
                            else -> dinnerMenu
                        }
                        showEditDialog = true
                    }
                )

                // 2. Dynamic Attendance Section
                QuickAttendanceCard(
                    mealType = currentMealType,
                    inputValue = studentNumberInput,
                    onValueChange = { studentNumberInput = it },
                    onMarkAttendance = { markAttendance() }
                )
                
                 // 3. Stats based on Repository
                // Calculate counts dynamically from repo
                val totalBf = StudentRepository.students.sumOf { it.breakfastCount }
                val totalLn = StudentRepository.students.sumOf { it.lunchCount }
                val totalDn = StudentRepository.students.sumOf { it.dinnerCount }
                
                var showResetDialog by remember { mutableStateOf(false) }

                AttendanceSummaryCard(
                    breakfast = totalBf, 
                    lunch = totalLn, 
                    dinner = totalDn,
                    onResetClick = { showResetDialog = true }
                )
                
                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text("Reset Live Overview") },
                        text = { Text("Are you sure you want to reset all live attendance counts to zero? This action cannot be undone.") },
                        confirmButton = {
                            Button(onClick = {
                                StudentRepository.resetLiveAttendance()
                                showResetDialog = false
                                scope.launch { snackbarHostState.showSnackbar("Live attendance reset to zero.") }
                            }) { Text("Confirm Reset") }
                        },
                        dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancel") } }
                    )
                }

                // 4. Total Banner
                TotalStudentsBanner(StudentRepository.students.size)
                
                // 5. Actions / Exports
                ExportActionsCard()
            }
        }
    }
    
    // Edit Dialog
    if (showEditDialog) {
        MenuEditDialog(
            mealType = editingMealType,
            initialValue = editingMenuValue,
            onDismiss = { showEditDialog = false },
            onSave = { newValue ->
                StudentRepository.updateMenu(editingMealType, newValue)
                showEditDialog = false
            }
        )
    }

    // Renew Dialog (Quick Action)
    if (showRenewDialogForStudent != null) {
        RenewPlanDialog(
            student = showRenewDialogForStudent!!,
            onDismiss = { showRenewDialogForStudent = null },
            onConfirm = { bf, ln, dn, amount ->
                StudentRepository.renewStudent(showRenewDialogForStudent!!.id, bf, ln, dn, amount)
                val sName = showRenewDialogForStudent!!.name
                showRenewDialogForStudent = null
                scope.launch {
                    snackbarHostState.showSnackbar("Plan Renewed for $sName. You can now mark attendance.")
                }
            }
        )
    }
}

// Helper to determine meal type based on time
fun getMealType(): String {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    
    val currentMinutes = hour * 60 + minute
    
    val bfStart = 6 * 60 // 06:00
    val bfEnd = 11 * 60 + 30 // 11:30
    val lunchEnd = 17 * 60 // 17:00
    
    return when {
        currentMinutes >= bfStart && currentMinutes < bfEnd -> "Breakfast"
        currentMinutes >= bfEnd && currentMinutes < lunchEnd -> "Lunch"
        else -> "Dinner"
    }
}

// --- Components ---

@Composable
fun MenuEditDialog(
    mealType: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit $mealType Menu") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Menu Items") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MenuCard(breakfast: String, lunch: String, dinner: String, onEditClick: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Today's Menu", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            MenuResCard("Breakfast", breakfast) { onEditClick("Breakfast") }
            Spacer(modifier = Modifier.height(8.dp))
            MenuResCard("Lunch", lunch) { onEditClick("Lunch") }
            Spacer(modifier = Modifier.height(8.dp))
            MenuResCard("Dinner", dinner) { onEditClick("Dinner") }
        }
    }
}

@Composable
fun MenuResCard(title: String, menu: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 4.dp)
    ) {
        Icon(Icons.Default.RestaurantMenu, null, tint = Color(0xFFFF6F00), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
            Text(menu, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        }
        Icon(Icons.Outlined.Edit, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun AttendanceSummaryCard(breakfast: Int, lunch: Int, dinner: Int, onResetClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Attendance Live Overview", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                IconButton(onClick = onResetClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Overview", tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                AttendanceStatItem(breakfast, "Breakfast")
                AttendanceStatItem(lunch, "Lunch")
                AttendanceStatItem(dinner, "Dinner")
            }
        }
    }
}

@Composable
fun AttendanceStatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFF5722)))
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
    }
}

@Composable
fun QuickAttendanceCard(
    mealType: String,
    inputValue: String,
    onValueChange: (String) -> Unit,
    onMarkAttendance: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Current Meal: $mealType",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputValue,
                onValueChange = onValueChange,
                placeholder = { Text("Enter Student ID (e.g. 4021)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onMarkAttendance,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Mark $mealType", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TotalStudentsBanner(count: Int) {
    Surface(
        color = Color(0xFFE0F2F1),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Groups, null, tint = Color(0xFF00695C))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Total Students Registered: $count",
                style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF00695C), fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ExportActionsCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF1E88E5), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reports & Exports",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    com.example.mymess.utils.CsvExporter.exportMasterReport(context, java.util.Calendar.getInstance()) 
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Outlined.TableView, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Master Report (Excel CSV)")
            }
        }
    }
}
