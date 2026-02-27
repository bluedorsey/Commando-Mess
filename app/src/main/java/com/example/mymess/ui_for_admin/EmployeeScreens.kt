package com.example.mymess.ui_for_admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.mymess.data.Employee
import com.example.mymess.data.EmployeeAdvance
import com.example.mymess.data.StudentRepository

@Composable
fun EmployeeListScreen(
    onEmployeeClick: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    // We add a refresh trigger here too, just in case adding an employee needs an immediate UI refresh
    var refreshTrigger by remember { mutableIntStateOf(0) }
    refreshTrigger // Observe for recomposition

    val employees = StudentRepository.employees

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Manage Employees",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(employees, key = { it.id }) { employee ->
                    EmployeeListItem(employee, onClick = { onEmployeeClick(employee.id) })
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFFFF5722),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
        }
    }

    if (showAddDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddDialog = false },
            onSave = {
                refreshTrigger++ // Refresh list after adding
            }
        )
    }
}

@Composable
fun EmployeeListItem(employee: Employee, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFF2196F3))
            ) {
                Text(
                    text = employee.name.firstOrNull()?.toString() ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(employee.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(employee.role, style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun AddEmployeeDialog(onDismiss: () -> Unit, onSave: () -> Unit = {}) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Employee") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role (e.g. Cook)") })
                OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            }
        },
        confirmButton = {
            Button(onClick = {
                if(name.isNotBlank() && role.isNotBlank()) {
                    StudentRepository.addEmployee(name, role, mobile)
                    onSave() // Trigger UI refresh
                    onDismiss()
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EmployeeProfileScreen(employeeId: String) {
    // TRIGGER ADDED HERE
    var refreshTrigger by remember { mutableIntStateOf(0) }
    refreshTrigger // Tells Compose to re-run this screen when incremented

    val employee = StudentRepository.employees.find { it.id == employeeId }
    val advances = StudentRepository.getEmployeeAdvances(employeeId)

    // Use the actual Total Advances value from the employee object, which is properly reset
    val totalAdvances = employee?.totalAdvances ?: 0.0
    
    // Net salary should be calculated
    val monthlySalary = employee?.monthlySalary ?: 0.0
    // If they took more advance than their salary, display a negative/zero appropriately. 
    // The user requested net salary = monthly salary - advance.
    val netSalary = monthlySalary - totalAdvances

    var showEditSalaryDialog by remember { mutableStateOf(false) }
    var showAddAdvanceDialog by remember { mutableStateOf(false) }
    var showPaySalaryDialog by remember { mutableStateOf(false) }

    var showEditEmployeeDialog by remember { mutableStateOf(false) }
    var showDeleteEmployeeDialog by remember { mutableStateOf(false) }

    if (employee == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Employee Not Found") }
        return
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF8F1))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    // Action Icons at Top Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showEditEmployeeDialog = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Employee", tint = Color.Gray)
                        }
                        IconButton(onClick = { showDeleteEmployeeDialog = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Employee", tint = Color.Red.copy(alpha=0.7f))
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF2196F3))
                    ) {
                        Text(
                            text = employee.name.firstOrNull()?.toString() ?: "",
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(employee.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                    Text(employee.role, style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF2196F3)))
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mobile", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                        Text(employee.mobile, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ID", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                        Text(employee.id, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Salary Details Card
            SalaryDetailsCard(
                monthlySalary = employee.monthlySalary,
                totalAdvances = totalAdvances,
                netSalary = netSalary,
                onEditClick = { showEditSalaryDialog = true },
                onPayClick = { showPaySalaryDialog = true }
            )

            // Advance Payments Card
            AdvancePaymentsCard(
                advances = advances,
                onAddClick = { showAddAdvanceDialog = true },
                onRevokeClick = { advanceId ->
                    StudentRepository.revokeEmployeeAdvance(advanceId)
                    refreshTrigger++ // Refresh after deleting an advance
                }
            )
        }
    }

    if (showEditSalaryDialog) {
        EditSalaryDialog(
            currentSalary = employee.monthlySalary,
            onDismiss = { showEditSalaryDialog = false },
            onSave = { newSalary ->
                StudentRepository.updateEmployeeSalary(employee.id, newSalary)
                showEditSalaryDialog = false
                refreshTrigger++ // Refresh after editing salary
            }
        )
    }

    if (showAddAdvanceDialog) {
        AddAdvanceDialog(
            onDismiss = { showAddAdvanceDialog = false },
            onSave = { amount, note ->
                StudentRepository.addEmployeeAdvance(employee.id, amount, note)
                showAddAdvanceDialog = false
                refreshTrigger++ // Refresh after adding advance
            }
        )
    }

    if (showPaySalaryDialog) {
        AlertDialog(
            onDismissRequest = { showPaySalaryDialog = false },
            title = { Text("Confirm Salary Payment") },
            text = { Text("Pay the net salary of ${formatCurrency(netSalary)} to ${employee.name}? This will record the payment and reset their total advances to zero.") },
            confirmButton = {
                Button(onClick = {
                    StudentRepository.payEmployeeSalary(employee.id)
                    showPaySalaryDialog = false
                    refreshTrigger++ // Refresh after paying salary
                }) { Text("Confirm Payment") }
            },
            dismissButton = { TextButton(onClick = { showPaySalaryDialog = false }) { Text("Cancel") } }
        )
    }

    if (showEditEmployeeDialog) {
        EditEmployeeDialog(
            employee = employee,
            onDismiss = { showEditEmployeeDialog = false },
            onSave = { name, role, mobile ->
                StudentRepository.updateEmployee(employee.id, name, role, mobile)
                showEditEmployeeDialog = false
                refreshTrigger++ // Refresh after editing details
            }
        )
    }

    if (showDeleteEmployeeDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteEmployeeDialog = false },
            title = { Text("Delete Employee") },
            text = { Text("Are you sure you want to delete ${employee.name}? This action cannot be undone and will delete all their advance records.") },
            confirmButton = {
                Button(
                    onClick = {
                        StudentRepository.deleteEmployee(employee.id)
                        showDeleteEmployeeDialog = false
                        // Note: Depending on your navigation setup, you might want to call a nav back function here instead of refreshing
                        refreshTrigger++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteEmployeeDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SalaryDetailsCard(monthlySalary: Double, totalAdvances: Double, netSalary: Double, onEditClick: () -> Unit, onPayClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Salary Details",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color(0xFFE8F5E9), // Light Green
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { onPayClick() }
                    ) {
                        Text(
                            text = "Give Salary",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        )
                    }
                    Surface(
                        color = Color(0xFFFFE0B2), // Light Orange
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { onEditClick() }
                    ) {
                        Text(
                            text = "Edit",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFFF6F00))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Monthly Salary
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Monthly Salary", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    Text(
                        text = formatCurrency(monthlySalary),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFF2E7D32), // Green
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row: Total Advances & Net Salary
            Row(modifier = Modifier.fillMaxWidth()) {
                // Total Advances
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Advances", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    Text(
                        text = formatCurrency(totalAdvances),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFD32F2F), // Red
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Net Salary
                Column(modifier = Modifier.weight(1f)) {
                    Text("Net Salary", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    Text(
                        text = formatCurrency(netSalary),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF2E7D32), // Green
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancePaymentsCard(advances: List<EmployeeAdvance>, onAddClick: () -> Unit, onRevokeClick: (String) -> Unit) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFFFF6F00),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Payment History",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    )
                }

                // Add Button
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = Color(0xFFFF6F00),
                    contentColor = Color.White,
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of Payments
            if (advances.isEmpty()) {
                Text("No payment history recorded.", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
            } else {
                advances.forEach { payment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (payment.isSalaryPayment) "Salary Payment" else "Advance",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (payment.isSalaryPayment) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatter.format(Date(payment.date)),
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                            if (payment.note.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = payment.note,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatCurrency(payment.amount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if(payment.isSalaryPayment) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Revoke button
                            IconButton(onClick = { onRevokeClick(payment.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Revoke", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Divider(color = Color.LightGray.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun EditSalaryDialog(currentSalary: Double, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var salaryStr by remember { mutableStateOf(if (currentSalary > 0) currentSalary.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Monthly Salary") },
        text = {
            OutlinedTextField(
                value = salaryStr,
                onValueChange = { salaryStr = it },
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val newSal = salaryStr.toDoubleOrNull() ?: 0.0
                onSave(newSal)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddAdvanceDialog(onDismiss: () -> Unit, onSave: (Double, String) -> Unit) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Advance Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    onSave(amount, note)
                }
            }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditEmployeeDialog(employee: Employee, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf(employee.name) }
    var role by remember { mutableStateOf(employee.role) }
    var mobile by remember { mutableStateOf(employee.mobile) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Employee Info") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && role.isNotBlank()) {
                    onSave(name, role, mobile)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val formattedStr = format.format(amount)
    
    // Replace standard ₹ sign and handle spacing issues for custom UI needs
    return formattedStr.replace("₹", "₹")
}