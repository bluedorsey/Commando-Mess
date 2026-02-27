package com.example.mymess.ui_for_admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymess.data.Student
import com.example.mymess.data.StudentRepository
import kotlinx.coroutines.launch

@Composable
fun StudentProfileScreen(
    studentId: String,
    onBackClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Lookup student from Repository (Live Update)
    val student = StudentRepository.students.find { it.id == studentId }

    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Student not found")
        }
        return
    }

    val totalMeals = student.breakfastCount + student.lunchCount + student.dinnerCount
    var showRenewDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profile Header Card
            ProfileDetailCard(student)
            
            // 2. Credits Card (New)
            CreditsCard(
                student = student,
                onRenew = { showRenewDialog = true }
            )

            // 3. Meal Summary Card
            SummaryCard(student, totalMeals)

            // 4. Payment/Renewal History (New)
            val paymentHistory = StudentRepository.getPaymentHistory(student.id)
            PaymentHistoryCard(paymentHistory)

            // 5. Attendance History Card
            HistoryCard(student)
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showRenewDialog) {
            RenewPlanDialog(
                student = student,
                onDismiss = { showRenewDialog = false },
                onConfirm = { bf, ln, dn, amount ->
                    StudentRepository.renewStudent(student.id, bf, ln, dn, amount)
                    showRenewDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Plan Renewed Successfully")
                    }
                }
            )
        }
    }
}

// --- Helper Components ---

@Composable
fun ProfileDetailCard(student: Student) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5722)) // Orange
            ) {
                Text(
                    text = student.name.firstOrNull()?.toString() ?: "",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Name
            Text(
                text = student.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Details
            Text(
                text = "ID: ${student.id}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            // Number removed as per request
            Text(
                text = "Mobile: ${student.mobile}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
        }
    }
}

@Composable
fun CreditsCard(student: Student, onRenew: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meal Credits",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                )
                Button(
                    onClick = onRenew,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Renew", fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                CreditItem("Breakfast", student.remainingBreakfasts.toString(), if(student.remainingBreakfasts <= 0) Color.Red else Color(0xFF1E293B))
                CreditItem("Lunch", student.remainingLunches.toString(), if(student.remainingLunches <= 0) Color.Red else Color(0xFF1E293B))
                CreditItem("Dinner", student.remainingDinners.toString(), if(student.remainingDinners <= 0) Color.Red else Color(0xFF1E293B))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                 CreditItem("Sunday Specials", student.remainingSundayMeals.toString(), Color(0xFFFFA000))
            }
        }
    }
}

@Composable
fun CreditItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = valueColor))
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
    }
}

@Composable
fun SummaryCard(student: Student, totalMeals: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "This Month's Usage",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(icon = Icons.Outlined.Coffee, count = student.breakfastCount, label = "Breakfast", color = Color(0xFFFF5722))
                StatItem(icon = Icons.Outlined.WbSunny, count = student.lunchCount, label = "Lunch", color = Color(0xFFFFA000))
                StatItem(icon = Icons.Outlined.Bedtime, count = student.dinnerCount, label = "Dinner", color = Color(0xFF8E24AA))
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Consumed:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
                Text(
                    text = totalMeals.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32) // Green
                    )
                )
            }
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )
    }
}

@Composable
fun PaymentHistoryCard(payments: List<com.example.mymess.data.PaymentRecord>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Receipt, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Payment / Renewal History",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (payments.isEmpty()) {
                Text("No payment history found.", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
            } else {
                payments.take(5).forEach { payment ->
                    PaymentItemRow(payment)
                    if (payment != payments.lastOrNull()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentItemRow(payment: com.example.mymess.data.PaymentRecord) {
    val formatter = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
    val dateStr = formatter.format(java.util.Date(payment.date))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Plan Renewal", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(text = dateStr, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
        }
        Text(
            text = "₹${payment.amount}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        )
    }
}

@Composable
fun RenewPlanDialog(student: Student, onDismiss: () -> Unit, onConfirm: (Int, Int, Int, Int) -> Unit) {
    var breakfastStr by remember { mutableStateOf("0") }
    var lunchStr by remember { mutableStateOf("0") }
    var dinnerStr by remember { mutableStateOf("0") }
    var amountStr by remember { mutableStateOf("") }
    
    val bfC = breakfastStr.toIntOrNull() ?: 0
    val lnC = lunchStr.toIntOrNull() ?: 0
    val dnC = dinnerStr.toIntOrNull() ?: 0
    val amt = amountStr.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renew Plan") },
        text = {
            Column {
                Text("Enter the number of meals to add and the payment amount.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = breakfastStr,
                    onValueChange = { breakfastStr = it },
                    label = { Text("Breakfast Meals") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                
                OutlinedTextField(
                    value = lunchStr,
                    onValueChange = { lunchStr = it },
                    label = { Text("Lunch Meals") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                
                OutlinedTextField(
                    value = dinnerStr,
                    onValueChange = { dinnerStr = it },
                    label = { Text("Dinner Meals") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(bfC, lnC, dnC, amt)
                },
                enabled = (bfC > 0 || lnC > 0 || dnC > 0) || amt > 0
            ) { Text("Confirm Renewal") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun HistoryCard(student: Student) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.DateRange, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Last Attendance",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (student.lastAttendanceDate == 0L) {
                 Text("No attendance recorded yet.", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
            } else {
                val formatter = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                val dateStr = formatter.format(java.util.Date(student.lastAttendanceDate))
                
                Column {
                    Text("Latest Activity", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray, fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryChip(icon: ImageVector, text: String) {
    Surface(
        color = Color(0xFFF5F6F8),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
        }
    }
}

