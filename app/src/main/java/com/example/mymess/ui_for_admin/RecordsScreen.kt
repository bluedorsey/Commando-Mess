package com.example.mymess.ui_for_admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymess.data.StudentRepository
import com.example.mymess.utils.CsvExporter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen() {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var searchQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    var viewMode by remember { mutableStateOf("Day") } 

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Records", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFF5722)),
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Options", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Download Today's Report") },
                                onClick = {
                                    showMenu = false
                                    CsvExporter.exportDailyReport(context, selectedDate)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Download Monthly Report") },
                                onClick = {
                                    showMenu = false
                                    CsvExporter.exportMonthlyReport(context, selectedDate)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            val allStudents = StudentRepository.students
            
            val studentsWithAttendance = remember(allStudents.size, allStudents.toList(), selectedDate) {
                allStudents.associateWith { student ->
                    val takenBf = StudentRepository.hasTakenMealOnDate(student.id, selectedDate.timeInMillis, "Breakfast")
                    val takenLn = StudentRepository.hasTakenMealOnDate(student.id, selectedDate.timeInMillis, "Lunch")
                    val takenDn = StudentRepository.hasTakenMealOnDate(student.id, selectedDate.timeInMillis, "Dinner")
                    Triple(takenBf, takenLn, takenDn)
                }
            }

            val searchedStudents = if (searchQuery.isBlank()) allStudents else allStudents.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery)
            }

            val studentsToShow = searchedStudents.filter { student ->
                val attendance = studentsWithAttendance[student]
                attendance != null && (attendance.first || attendance.second || attendance.third)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFFF5722),
                                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                            )
                            .padding(bottom = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            ViewModeToggle(
                                selectedMode = viewMode,
                                onModeSelected = { viewMode = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        MonthYearSelector(
                            currentDate = selectedDate,
                            onPreviousMonth = {
                                val newCal = selectedDate.clone() as Calendar
                                newCal.add(Calendar.MONTH, -1)
                                selectedDate = newCal
                            },
                            onNextMonth = {
                                val newCal = selectedDate.clone() as Calendar
                                newCal.add(Calendar.MONTH, 1)
                                selectedDate = newCal
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                        ) {
                            CalendarGrid(
                                currentDate = selectedDate,
                                onDateSelected = { newDate ->
                                    selectedDate = newDate
                                }
                            )
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            "STUDENT DATA",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by Name or ID") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color(0xFFFF5722)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Student Name", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("B", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                            Text("L", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                            Text("D", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (studentsToShow.isEmpty()) {
                    item {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp), contentAlignment = Alignment.Center) {
                            
                            if (searchQuery.isNotBlank() && searchedStudents.isNotEmpty()) {
                                Text("This student hasn't taken a meal for it", color = Color.Gray)
                            } else {
                                Text("No records found", color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(studentsToShow) { student ->
                         val attendance = studentsWithAttendance[student] ?: Triple(false, false, false)
                         Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                             StudentAttendanceRowCached(
                                 student = student, 
                                 takenBf = attendance.first, 
                                 takenLn = attendance.second, 
                                 takenDn = attendance.third
                             )
                             Spacer(modifier = Modifier.height(8.dp))
                         }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun ViewModeToggle(selectedMode: String, onModeSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .background(Color(0xFFFF7043), RoundedCornerShape(20.dp))
            .padding(4.dp)
    ) {
        ToggleButton(text = "Day", selected = selectedMode == "Day") { onModeSelected("Day") }
        ToggleButton(text = "Month", selected = selectedMode == "Month") { onModeSelected("Month") }
    }
}

@Composable
fun ToggleButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFFFFAB91) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MonthYearSelector(currentDate: Calendar, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            Text(
                monthFormat.format(currentDate.time),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Text(
                yearFormat.format(currentDate.time),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
            )
        }

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
        }
    }
}

@Composable
fun CalendarGrid(currentDate: Calendar, onDateSelected: (Calendar) -> Unit) {
    val daysInMonth = remember(currentDate.timeInMillis) {
        getDaysInMonth(currentDate)
    }
    
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekDays.forEach { day ->
                Text(
                    day, 
                    modifier = Modifier.weight(1f), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
             columns = GridCells.Fixed(7),
             modifier = Modifier.height(200.dp), 
             userScrollEnabled = false
        ) {
             val firstDayOfWeek = daysInMonth.firstOrNull()?.get(Calendar.DAY_OF_WEEK) ?: Calendar.MONDAY
             val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
             
             items(offset) {
                 Spacer(modifier = Modifier.size(30.dp))
             }
             
             items(daysInMonth) { date ->
                 val isSelected = isSameDay(date, currentDate)
                 val dayNum = date.get(Calendar.DAY_OF_MONTH).toString()
                 
                 Box(
                     modifier = Modifier
                         .size(40.dp)
                         .clip(CircleShape)
                         .background(if (isSelected) Color(0xFFFF5722) else Color.Transparent)
                         .clickable { onDateSelected(date) },
                     contentAlignment = Alignment.Center
                 ) {
                     Text(
                         dayNum,
                         color = if (isSelected) Color.White else Color.Black,
                         fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                     )
                 }
             }
        }
    }
}

@Composable
fun StudentAttendanceRow(student: com.example.mymess.data.Student, date: Calendar) {
    val dateInMillis = date.timeInMillis
    
    val takenBf = StudentRepository.hasTakenMealOnDate(student.id, dateInMillis, "Breakfast")
    val takenLn = StudentRepository.hasTakenMealOnDate(student.id, dateInMillis, "Lunch")
    val takenDn = StudentRepository.hasTakenMealOnDate(student.id, dateInMillis, "Dinner")
    
    StudentAttendanceRowCached(student, takenBf, takenLn, takenDn)
}

@Composable
fun StudentAttendanceRowCached(
    student: com.example.mymess.data.Student,
    takenBf: Boolean,
    takenLn: Boolean,
    takenDn: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
             Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF5C6BC0),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            student.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(student.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Text("ID: ${student.id} | B:${student.remainingBreakfasts} L:${student.remainingLunches} D:${student.remainingDinners}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusItem("Breakfast", takenBf)
                StatusItem("Lunch", takenLn)
                StatusItem("Dinner", takenDn)
            }
        }
    }
}

@Composable
fun StatusItem(label: String, present: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (present) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (present) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (present) Color(0xFF4CAF50) else Color(0xFFE53935),
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = Color.Gray))
    }
}

@Composable
fun StatusIcon(present: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (present) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (present) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (present) Color(0xFF4CAF50) else Color(0xFFE53935),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun DownloadButton(text: String, containerColor: Color, textColor: Color = Color.White) {
    Button(
        onClick = {},
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(8.dp),
        border = if (containerColor == Color.White) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray) else null,
        elevation = if(containerColor == Color.White) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(0.dp)
    ) {
        Icon(Icons.Default.Download, null, tint = textColor)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = textColor, fontWeight = FontWeight.Bold)
    }
}


fun getDaysInMonth(currentDate: Calendar): List<Calendar> {
    val days = mutableListOf<Calendar>()
    val cal = currentDate.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    for (i in 1..maxDay) {
        days.add(cal.clone() as Calendar)
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return days
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
