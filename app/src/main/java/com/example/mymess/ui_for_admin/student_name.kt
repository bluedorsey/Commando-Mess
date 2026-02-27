package com.example.mymess.ui_for_admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymess.data.Student
import com.example.mymess.data.StudentRepository

@Composable
fun NameOfStudentScreen(
    onStudentClick: (String) -> Unit
) {
    // --- State ---
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // These states hold the specific student object being acted upon. If null, dialog is hidden.
    var selectedStudentForOptions by remember { mutableStateOf<Student?>(null) }
    var showEditDialog by remember { mutableStateOf<Student?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Student?>(null) }

    // --- Data ---
    // Note: Ideally, this comes from a ViewModel observing a Flow/LiveData from the Repository.
    // For now, we fetch the list.
    val allStudents = remember { StudentRepository.students }

    // Filter logic
    val filteredStudents = remember(searchQuery, allStudents) {
        if (searchQuery.isBlank()) allStudents
        else allStudents.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.mobile.contains(searchQuery)
        }
    }

    // --- UI Layout ---
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFFF5722),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or mobile...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // The List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
            ) {
                items(filteredStudents, key = { it.id }) { student ->
                    StudentListItem(
                        student = student,
                        onClick = { onStudentClick(student.id) },
                        onLongClick = { selectedStudentForOptions = student }
                    )
                }
            }
        }
    }

    // --- Dialogs ---

    // 1. Add Student Dialog
    if (showAddDialog) {
        AddStudentDialog(onDismiss = { showAddDialog = false })
    }

    // 2. Options Dialog (appears on long press)
    if (selectedStudentForOptions != null) {
        val student = selectedStudentForOptions!!
        AlertDialog(
            onDismissRequest = { selectedStudentForOptions = null },
            title = { Text("Manage ${student.name}") },
            text = { Text("What would you like to do?") },
            confirmButton = {
                Button(onClick = {
                    val temp = selectedStudentForOptions
                    selectedStudentForOptions = null
                    showEditDialog = temp
                }) { Text("Edit Details") }
            },
            dismissButton = {
                Button(
                    onClick = {
                        val temp = selectedStudentForOptions
                        selectedStudentForOptions = null
                        showDeleteDialog = temp
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete Student") }
            }
        )
    }

    // 3. Edit Student Dialog
    if (showEditDialog != null) {
        EditStudentDialog(
            student = showEditDialog!!,
            onDismiss = { showEditDialog = null }
        )
    }

    // 4. Delete Confirmation Dialog
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Remove Student?") },
            text = { Text("Are you sure you want to remove ${showDeleteDialog?.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog?.let { StudentRepository.removeStudent(it.id) }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Confirm Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentListItem(student: Student, onClick: () -> Unit, onLongClick: () -> Unit) {
    // Red Card Logic: if all balances are <= 0
    val isLowBalance = student.remainingBreakfasts <= 0 &&
            student.remainingLunches <= 0 &&
            student.remainingDinners <= 0

    val cardColor = if (isLowBalance) Color(0xFFFFEBEE) else Color.White
    val strokeColor = if (isLowBalance) Color.Red else Color.Transparent

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = if (isLowBalance) BorderStroke(1.dp, strokeColor) else null,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isLowBalance) Color.Red else Color(0xFFFF5722))
            ) {
                Text(
                    text = student.name.firstOrNull()?.toString()?.uppercase() ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: ${student.id}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
                if (isLowBalance) {
                    Text(
                        text = "Low / No Credits!",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    Text(
                        text = "B: ${student.remainingBreakfasts} | L: ${student.remainingLunches} | D: ${student.remainingDinners}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun AddStudentDialog(onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var breakfastStr by remember { mutableStateOf("0") }
    var lunchStr by remember { mutableStateOf("0") }
    var dinnerStr by remember { mutableStateOf("0") }
    var amountStr by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Student") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { if (it.length <= 10) mobile = it },
                    label = { Text("Mobile (10 Digits)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                OutlinedTextField(
                    value = breakfastStr,
                    onValueChange = { breakfastStr = it },
                    label = { Text("Breakfast Meals") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = lunchStr,
                    onValueChange = { lunchStr = it },
                    label = { Text("Lunch Meals") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = dinnerStr,
                    onValueChange = { dinnerStr = it },
                    label = { Text("Dinner Meals") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount Paid (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bfCount = breakfastStr.toIntOrNull() ?: 0
                    val lnCount = lunchStr.toIntOrNull() ?: 0
                    val dnCount = dinnerStr.toIntOrNull() ?: 0
                    val amt = amountStr.toIntOrNull() ?: 0

                    if (name.isBlank() || mobile.length != 10) {
                        errorMessage = "Please enter valid details"
                        return@Button
                    }
                    val result = StudentRepository.addStudent(name, mobile, bfCount, lnCount, dnCount, amt)
                    if (result.isSuccess) {
                        onDismiss()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Error adding student"
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditStudentDialog(student: Student, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(student.name) }
    var mobile by remember { mutableStateOf(student.mobile) }
    var breakfastStr by remember { mutableStateOf(student.remainingBreakfasts.toString()) }
    var lunchStr by remember { mutableStateOf(student.remainingLunches.toString()) }
    var dinnerStr by remember { mutableStateOf(student.remainingDinners.toString()) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { if (it.length <= 10) mobile = it },
                    label = { Text("Mobile (10 Digits)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = breakfastStr,
                    onValueChange = { breakfastStr = it },
                    label = { Text("Breakfast Meals") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = lunchStr,
                    onValueChange = { lunchStr = it },
                    label = { Text("Lunch Meals") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = dinnerStr,
                    onValueChange = { dinnerStr = it },
                    label = { Text("Dinner Meals") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bfCount = breakfastStr.toIntOrNull() ?: 0
                    val lnCount = lunchStr.toIntOrNull() ?: 0
                    val dnCount = dinnerStr.toIntOrNull() ?: 0

                    val result = StudentRepository.updateStudent(student.id, name, mobile, bfCount, lnCount, dnCount)
                    if (result.isSuccess) {
                        onDismiss()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Error updating student"
                    }
                }
            ) { Text("Save Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewNameOfStudentScreen() {
    NameOfStudentScreen(onStudentClick = {})
}