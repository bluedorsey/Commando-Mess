package com.example.mymess.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.DocumentId
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max


data class Student(
    var id: String = "",
    
    @DocumentId
    var docId: String = "",
    
    var name: String = "",
    
    var mobile: String = "",
    
    var remainingBreakfasts: Int = 0,
    
    var remainingLunches: Int = 0,
    
    var remainingDinners: Int = 0,
    
    var remainingSundayMeals: Int = 4,

    var breakfastCount: Int = 0,
    var lunchCount: Int = 0,
    var dinnerCount: Int = 0,

    var lastAttendanceDate: Long = 0,
    var lastBreakfastDate: Long = 0,
    var lastLunchDate: Long = 0,
    var lastDinnerDate: Long = 0,
    
    var lastMealTook: String = "Never"
)

data class Employee(
    var id: String = "",
    @get:PropertyName("Name")
    @set:PropertyName("Name")
    var name: String = "",
    @get:PropertyName("Role")
    @set:PropertyName("Role")
    var role: String = "",
    @get:PropertyName("Phone Number")
    @set:PropertyName("Phone Number")
    var mobile: String = "",
    @get:PropertyName("Monthly Salary")
    @set:PropertyName("Monthly Salary")
    var monthlySalary: Double = 0.0,
    @get:PropertyName("Total Advances")
    @set:PropertyName("Total Advances")
    var totalAdvances: Double = 0.0
) {
    constructor() : this("", "", "", "", 0.0, 0.0)
}

data class EmployeeAdvance(
    @DocumentId var id: String = "",
    var employeeId: String = "",
    var amount: Double = 0.0,
    var date: Long = 0,
    var note: String = "",
    var isSalaryPayment: Boolean = false 
)

data class PaymentRecord(
    val id: String = "",
    val studentId: String = "",
    val amount: Int = 0,
    val date: Long = 0,
    val method: String = "Cash"
)

data class AttendanceLog(
    val studentId: String = "",
    val timestamp: Long = 0,
    val mealType: String = ""
)
object StudentRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private var context: android.content.Context? = null

    private var studentsListener: ListenerRegistration? = null
    private var employeesListener: ListenerRegistration? = null
    private var advancesListener: ListenerRegistration? = null
    private var paymentsListener: ListenerRegistration? = null
    private var logsListener: ListenerRegistration? = null
    private var menuListener: ListenerRegistration? = null

    private val _students = mutableStateListOf<Student>()
    val students: List<Student> get() = _students

    private val _employees = mutableStateListOf<Employee>()
    val employees: List<Employee> get() = _employees
    
    private val _employeeAdvances = mutableStateListOf<EmployeeAdvance>()
    val employeeAdvances: List<EmployeeAdvance> get() = _employeeAdvances


    private val _payments = mutableStateListOf<PaymentRecord>()
    val payments: List<PaymentRecord> get() = _payments

    private val _attendanceLogs = mutableStateListOf<AttendanceLog>()
    val attendanceLogs: List<AttendanceLog> get() = _attendanceLogs


    private val _archivedStudents = mutableListOf<Student>()

    var breakfastMenu by mutableStateOf("")
        private set
    var lunchMenu by mutableStateOf("")
        private set
    var dinnerMenu by mutableStateOf("")
        private set

    fun init(context: android.content.Context) {
        this.context = context
        setupSnapshotListeners()
    }

    private fun setupSnapshotListeners() {
        studentsListener = firestore.collection("Student_detail")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _students.clear()
                    for (doc in snapshot.documents) {
                        try {
                            doc.toObject(Student::class.java)?.let { student ->
                                if (student.name.isBlank()) student.name = doc.id
                                _students.add(student)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        employeesListener = firestore.collection("Employee")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _employees.clear()
                    for (doc in snapshot.documents) {
                        try {
                            val emp = doc.toObject(Employee::class.java)
                            if (emp != null) {
                                emp.id = doc.id 
                                _employees.add(emp)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            
        advancesListener = firestore.collection("employeeAdvances")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _employeeAdvances.clear()
                    for (doc in snapshot.documents) {
                        try {
                            doc.toObject(EmployeeAdvance::class.java)?.let { _employeeAdvances.add(it) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        paymentsListener = firestore.collection("payments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _payments.clear()
                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val studentId = doc.getString("studentId") ?: ""
                            val amount = doc.getLong("amount")?.toInt() ?: 0
                            val method = doc.getString("method") ?: "Cash"
                            
                            val dateObj = doc.get("date")
                            val date: Long = when (dateObj) {
                                is Long -> dateObj
                                is com.google.firebase.Timestamp -> dateObj.toDate().time
                                is java.util.Date -> dateObj.time
                                else -> 0L
                            }
                            
                            _payments.add(PaymentRecord(id, studentId, amount, date, method))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        logsListener = firestore.collection("attendanceLogs")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    _attendanceLogs.clear()
                    for (doc in snapshot.documents) {
                        try {
                            val studentId = doc.getString("studentId") ?: ""
                            val mealType = doc.getString("mealType") ?: ""
                            
                            val timeObj = doc.get("timestamp")
                            val timestamp: Long = when (timeObj) {
                                is Long -> timeObj
                                is com.google.firebase.Timestamp -> timeObj.toDate().time
                                is java.util.Date -> timeObj.time
                                else -> 0L
                            }
                            
                            _attendanceLogs.add(AttendanceLog(studentId, timestamp, mealType))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        menuListener = firestore.collection("TodaysMenu").document("menu")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    breakfastMenu = snapshot.getString("breakfast") ?: "Aloo Paratha, Curd, Tea"
                    lunchMenu = snapshot.getString("lunch") ?: "Rice, Dal Fry, Seasonal Veg"
                    dinnerMenu = snapshot.getString("dinner") ?: "Paneer, Roti, Rice"
                } else {
                    val defaultMenu = mapOf(
                        "breakfast" to "Aloo Paratha, Curd, Tea",
                        "lunch" to "Rice, Dal Fry, Seasonal Veg",
                        "dinner" to "Paneer, Roti, Rice"
                    )
                    firestore.collection("TodaysMenu").document("menu").set(defaultMenu)
                }
            }
    }



    fun getStudent(id: String): Student? {
        return _students.find { it.id == id || it.name == id }
    }


    fun addStudent(name: String, mobile: String, breakfastCount: Int, lunchCount: Int, dinnerCount: Int, amount: Int): Result<String> {
        if (name.isBlank()) return Result.failure(Exception("Name cannot be empty"))
        if (mobile.length != 10) return Result.failure(Exception("Mobile must be 10 digits"))

        if (isMobileDuplicate(mobile)) {
            return Result.failure(Exception("Mobile already exists"))
        }

        if (_students.any { it.name.equals(name.trim(), ignoreCase = true) }) {
            return Result.failure(Exception("Student with this name already exists"))
        }

        val sun = dinnerCount / 7

        val newId = generateUniqueId() 
        val docId = name.trim() 
        
        val newStudent = Student(
            id = newId, 
            docId = docId, 
            name = name,
            mobile = mobile,
            remainingBreakfasts = breakfastCount,
            remainingLunches = lunchCount,
            remainingDinners = dinnerCount,
            remainingSundayMeals = sun,
            lastMealTook = "Never"
        )

        val batch = firestore.batch()
        val studentRef = firestore.collection("Student_detail").document(docId)
        batch.set(studentRef, newStudent)

        if (amount > 0) {
            val paymentId = "${name}_${System.currentTimeMillis()}"
            val paymentData = hashMapOf(
                "id" to paymentId,
                "studentId" to newId,
                "amount" to amount,
                "date" to java.util.Date(),
                "method" to "Initial Payment"
            )
            val paymentRef = firestore.collection("payments").document(paymentId)
            batch.set(paymentRef, paymentData)
        }

        batch.commit()
            .addOnFailureListener { e -> 
               e.printStackTrace()
            }
        return Result.success("Student Added")
    }

    fun updateStudent(id: String, name: String, mobile: String, breakfastCount: Int, lunchCount: Int, dinnerCount: Int, consumedBreakfasts: Int = -1, consumedLunches: Int = -1, consumedDinners: Int = -1, sundayMeals: Int = -1): Result<String> {
        val student = _students.find { it.id == id || it.name == id }
        if (student == null) return Result.failure(Exception("Student not found"))

        if (name.isBlank()) return Result.failure(Exception("Name cannot be empty"))
        if (mobile.length != 10) return Result.failure(Exception("Invalid Mobile"))

        if (isMobileDuplicate(mobile, excludeId = student.id)) {
            return Result.failure(Exception("Mobile already exists"))
        }

        val updates = mutableMapOf<String, Any>(
            "name" to name, 
            "mobile" to mobile,
            "remainingBreakfasts" to breakfastCount,
            "remainingLunches" to lunchCount,
            "remainingDinners" to dinnerCount
        )

        if (consumedBreakfasts >= 0) updates["breakfastCount"] = consumedBreakfasts
        if (consumedLunches >= 0) updates["lunchCount"] = consumedLunches
        if (consumedDinners >= 0) updates["dinnerCount"] = consumedDinners
        if (sundayMeals >= 0) updates["remainingSundayMeals"] = sundayMeals

        firestore.collection("Student_detail").document(student.docId)
            .update(updates).addOnFailureListener { e -> e.printStackTrace() }
        return Result.success("Updated")
    }

    fun removeStudent(studentId: String) {
        val student = _students.find { it.id == studentId || it.name == studentId }
        if (student != null) {
            _archivedStudents.add(student) 
            firestore.collection("Student_detail").document(student.docId).delete()
                .addOnFailureListener { e -> e.printStackTrace() }
        }
    }


    fun hasTakenMealOnDate(studentId: String, date: Long, mealType: String): Boolean {
        
        val callDate = Calendar.getInstance().apply { timeInMillis = date }
        val callDay = callDate.get(Calendar.DAY_OF_YEAR)
        val callYear = callDate.get(Calendar.YEAR)

        val student = _students.find { it.id == studentId || it.name == studentId || it.docId == studentId }
        val searchName = student?.name ?: studentId

        return _attendanceLogs.any { log ->
            val matchId = log.studentId == studentId || log.studentId == searchName || (student != null && log.studentId == student.id)
            if (!matchId || log.mealType != mealType) return@any false
            
            val logDate = Calendar.getInstance().apply { timeInMillis = log.timestamp }
            logDate.get(Calendar.DAY_OF_YEAR) == callDay && logDate.get(Calendar.YEAR) == callYear
        }
    }

    fun getAttendanceForDate(dateInMillis: Long): List<AttendanceLog> {
        val callDate = Calendar.getInstance().apply { timeInMillis = dateInMillis }
        val callDay = callDate.get(Calendar.DAY_OF_YEAR)
        val callYear = callDate.get(Calendar.YEAR)

        return _attendanceLogs.filter { log ->
            val logDate = Calendar.getInstance().apply { timeInMillis = log.timestamp }
            logDate.get(Calendar.DAY_OF_YEAR) == callDay && logDate.get(Calendar.YEAR) == callYear
        }
    }
    
    val todaysAttendanceLogs: List<AttendanceLog> get() {
        val now = System.currentTimeMillis()
        return getAttendanceForDate(now)
    }

    fun markAttendance(studentId: String, mealType: String): Result<String> {
        val student = _students.find { it.id == studentId || it.name == studentId } ?: return Result.failure(Exception("Student not found"))
        
        if (hasTakenMealOnDate(studentId, System.currentTimeMillis(), mealType)) {
             return Result.failure(Exception("Already marked for $mealType today"))
        }

        val calendar = Calendar.getInstance()
        val isSunday = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

        var s = student.copy()
        var successMessage = "Attendance Marked for $mealType"

        when(mealType) {
            "Breakfast" -> {
                if (s.remainingBreakfasts <= 0) return Result.failure(Exception("No Breakfast credits left"))
                s.remainingBreakfasts--
                s.breakfastCount++
                s.lastBreakfastDate = System.currentTimeMillis()
            }
            "Lunch" -> {
                if (s.remainingLunches <= 0) return Result.failure(Exception("No Lunch credits left"))
                s.remainingLunches--
                s.lunchCount++
                s.lastLunchDate = System.currentTimeMillis()
            }
            "Dinner" -> {
                 if (isSunday) {
                     if (s.remainingSundayMeals > 0 && s.remainingDinners > 0) {
                         s.remainingSundayMeals--
                         s.remainingDinners--
                         successMessage += " (Sunday Special)"
                     } else {
                         return Result.failure(Exception("Not enough Sunday/Dinner credits left"))
                     }
                 } else {
                     if (s.remainingDinners <= 0) return Result.failure(Exception("No Dinner credits left"))
                     s.remainingDinners--
                 }
                 
                 s.dinnerCount++
                 s.lastDinnerDate = System.currentTimeMillis()
            }
        }

        s.lastAttendanceDate = System.currentTimeMillis()
        
        val sdf = SimpleDateFormat("dd MMMM yyyy 'at' HH:mm:ss", Locale.getDefault())
        s.lastMealTook = sdf.format(Date(s.lastAttendanceDate))

        val batch = firestore.batch()
        val studentRef = firestore.collection("Student_detail").document(student.docId)
        batch.set(studentRef, s)

        val logId = "${student.name}_${System.currentTimeMillis()}"
        val logRef = firestore.collection("attendanceLogs").document(logId)
        val logData = hashMapOf(
            "studentId" to student.id,
            "timestamp" to java.util.Date(), 
            "mealType" to mealType
        )
        batch.set(logRef, logData)

        batch.commit()
            .addOnFailureListener { e -> e.printStackTrace() }
        
        return Result.success(successMessage)
    }

    fun markHardcoreAttendance(studentId: String, mealType: String): Result<String> {
        val student = _students.find { it.id == studentId || it.name == studentId } ?: return Result.failure(Exception("Student not found"))
        
        if (hasTakenMealOnDate(studentId, System.currentTimeMillis(), mealType)) {
             return Result.failure(Exception("Already marked for $mealType today"))
        }

        var s = student.copy()
        var successMessage = "Hardcore Attendance Marked for $mealType"

        fun deductCredit(): String? {
            if (mealType == "Breakfast") {
                if (s.remainingBreakfasts > 0) { s.remainingBreakfasts--; return "Breakfast" }
                if (s.remainingLunches > 0) { s.remainingLunches--; return "Lunch" }
                if (s.remainingDinners > 0) { s.remainingDinners--; return "Dinner" }
            } else if (mealType == "Lunch") {
                if (s.remainingLunches > 0) { s.remainingLunches--; return "Lunch" }
                if (s.remainingDinners > 0) { s.remainingDinners--; return "Dinner" }
                if (s.remainingBreakfasts > 0) { s.remainingBreakfasts--; return "Breakfast" }
            } else if (mealType == "Dinner") {
                if (s.remainingDinners > 0) { s.remainingDinners--; return "Dinner" }
                if (s.remainingLunches > 0) { s.remainingLunches--; return "Lunch" }
                if (s.remainingBreakfasts > 0) { s.remainingBreakfasts--; return "Breakfast" }
            }
            return null
        }

        val deductedFrom = deductCredit()
        if (deductedFrom == null) {
            return Result.failure(Exception("No credits left to deduct!"))
        }

        when(mealType) {
            "Breakfast" -> {
                s.breakfastCount++
                s.lastBreakfastDate = System.currentTimeMillis()
            }
            "Lunch" -> {
                s.lunchCount++
                s.lastLunchDate = System.currentTimeMillis()
            }
            "Dinner" -> {
                 s.dinnerCount++
                 s.lastDinnerDate = System.currentTimeMillis()
            }
        }

        if (deductedFrom != mealType) {
            successMessage += " (Deducted from $deductedFrom)"
        }

        s.lastAttendanceDate = System.currentTimeMillis()
        
        val sdf = SimpleDateFormat("dd MMMM yyyy 'at' HH:mm:ss", Locale.getDefault())
        s.lastMealTook = sdf.format(Date(s.lastAttendanceDate))

        val batch = firestore.batch()
        val studentRef = firestore.collection("Student_detail").document(student.docId)
        batch.set(studentRef, s)

        val logId = "${student.name}_${System.currentTimeMillis()}"
        val logRef = firestore.collection("attendanceLogs").document(logId)
        val logData = hashMapOf(
            "studentId" to student.id,
            "timestamp" to java.util.Date(), 
            "mealType" to mealType
        )
        batch.set(logRef, logData)

        batch.commit()
            .addOnFailureListener { e -> e.printStackTrace() }
        
        return Result.success(successMessage)
    }

    fun resetLiveAttendance() {
        _students.forEach { 
            it.breakfastCount = 0
            it.lunchCount = 0
            it.dinnerCount = 0
        }
        
        val batch = firestore.batch()
        _students.forEach { student ->
            val ref = firestore.collection("Student_detail").document(student.docId)
            batch.update(
                ref,
                "breakfastCount", 0,
                "lunchCount", 0,
                "dinnerCount", 0
            )
        }
        
        batch.commit()
            .addOnFailureListener { e -> e.printStackTrace() }
    }


    fun renewStudent(studentId: String, breakfastCount: Int, lunchCount: Int, dinnerCount: Int, amount: Int) {
         val student = _students.find { it.id == studentId || it.name == studentId } ?: return
         val s = student.copy()
         
         s.remainingBreakfasts += breakfastCount
         s.remainingLunches += lunchCount
         s.remainingDinners += dinnerCount
         
         if (dinnerCount > 0) {
             s.remainingSundayMeals += (dinnerCount / 7)
         }

         s.breakfastCount = 0
         s.lunchCount = 0
         s.dinnerCount = 0
         
         val batch = firestore.batch()
         val studentRef = firestore.collection("Student_detail").document(student.docId)
         batch.set(studentRef, s)
         
         val paymentId = "${student.name}_${System.currentTimeMillis()}"
         val paymentData = hashMapOf(
             "id" to paymentId,
             "studentId" to student.id,
             "amount" to amount,
             "date" to java.util.Date(),
             "method" to "Renewal"
         )
         val paymentRef = firestore.collection("payments").document(paymentId)
         batch.set(paymentRef, paymentData)
         
         batch.commit()
             .addOnFailureListener { e -> e.printStackTrace() }
    }
    
    fun getPaymentHistory(studentId: String): List<PaymentRecord> {
        val student = _students.find { it.id == studentId || it.name == studentId || it.docId == studentId }
        val searchName = student?.name ?: studentId
        return _payments.filter { 
            it.studentId == studentId || it.studentId == searchName || (student != null && it.studentId == student.id)
        }.sortedByDescending { it.date }
    }


    fun updateMenu(mealType: String, newMenu: String) {
        val update = mapOf(mealType.lowercase() to newMenu)
        firestore.collection("TodaysMenu").document("menu")
            .set(update, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { e -> e.printStackTrace() }
    }

    fun addEmployee(name: String, role: String, mobile: String): Result<String> {
        if (name.isBlank() || role.isBlank()) return Result.failure(Exception("Invalid details"))
        val newId = UUID.randomUUID().toString()
        val newEmployee = Employee(newId, name, role, mobile, 0.0)
        firestore.collection("Employee").document(newId).set(newEmployee)
        return Result.success("Employee Added")
    }

    fun updateEmployee(employeeId: String, name: String, role: String, mobile: String): Result<String> {
        if (name.isBlank() || role.isBlank()) return Result.failure(Exception("Invalid details"))
        val updateData = mapOf(
            "Name" to name,
            "Role" to role,
            "Phone Number" to mobile
        )
        firestore.collection("Employee").document(employeeId)
            .set(updateData, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { it.printStackTrace() }
        return Result.success("Employee Updated")
    }

    fun deleteEmployee(employeeId: String) {
        val batch = firestore.batch()
        
        val employeeRef = firestore.collection("Employee").document(employeeId)
        batch.delete(employeeRef)
        
        _employeeAdvances.filter { it.employeeId == employeeId }.forEach { advance ->
            val advanceRef = firestore.collection("employeeAdvances").document(advance.id)
            batch.delete(advanceRef)
        }
        
        batch.commit()
            .addOnFailureListener { it.printStackTrace() }
    }

    fun updateEmployeeSalary(employeeId: String, newSalary: Double) {
        val updateData = mapOf("Monthly Salary" to newSalary)
        firestore.collection("Employee").document(employeeId)
            .set(updateData, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { it.printStackTrace() }
    }

    fun addEmployeeAdvance(employeeId: String, amount: Double, note: String) {
        val newId = UUID.randomUUID().toString()
        val newAdvance = EmployeeAdvance(
            id = newId,
            employeeId = employeeId,
            amount = amount,
            date = System.currentTimeMillis(),
            note = note
        )
        
        val employee = _employees.find { it.id == employeeId }
        val currentAdvances = employee?.totalAdvances ?: 0.0
        val newTotal = currentAdvances + amount

        val batch = firestore.batch()
        
        val advanceRef = firestore.collection("employeeAdvances").document(newId)
        batch.set(advanceRef, newAdvance)
        
        val employeeRef = firestore.collection("Employee").document(employeeId)
        batch.update(employeeRef, "Total Advances", newTotal)

        batch.commit()
            .addOnFailureListener { it.printStackTrace() }
    }

    fun revokeEmployeeAdvance(advanceId: String) {
        val advance = _employeeAdvances.find { it.id == advanceId } ?: return
        val employeeId = advance.employeeId
        val amount = advance.amount
        
        val employee = _employees.find { it.id == employeeId }
        val currentAdvances = employee?.totalAdvances ?: 0.0
        val newTotal = maxOf(0.0, currentAdvances - amount)

        val batch = firestore.batch()
        
        val advanceRef = firestore.collection("employeeAdvances").document(advanceId)
        batch.delete(advanceRef)
        
        val employeeRef = firestore.collection("Employee").document(employeeId)
        batch.update(employeeRef, "Total Advances", newTotal)

        batch.commit()
            .addOnFailureListener { it.printStackTrace() }
    }
    
    fun payEmployeeSalary(employeeId: String) {
        val employee = _employees.find { it.id == employeeId } ?: return
        val currentAdvances = employee.totalAdvances
        val monthlySalary = employee.monthlySalary
        val netSalary = maxOf(0.0, monthlySalary - currentAdvances)

        val newId = UUID.randomUUID().toString()
        val salaryPaymentRecord = EmployeeAdvance(
            id = newId,
            employeeId = employeeId,
            amount = netSalary, 
            date = System.currentTimeMillis(),
            note = "Salary Paid",
            isSalaryPayment = true
        )

        val batch = firestore.batch()

        val advanceRef = firestore.collection("employeeAdvances").document(newId)
        batch.set(advanceRef, salaryPaymentRecord)

        val employeeRef = firestore.collection("Employee").document(employeeId)
        batch.update(employeeRef, "Total Advances", 0.0)

        batch.commit()
            .addOnFailureListener { it.printStackTrace() }
    }
    
    fun getEmployeeAdvances(employeeId: String): List<EmployeeAdvance> {
        return _employeeAdvances.filter { it.employeeId == employeeId }.sortedByDescending { it.date }
    }


    private fun generateUniqueId(): String {
        var id: String
        do {
            id = kotlin.random.Random.nextInt(1000, 9999).toString()
        } while (_students.any { it.id == id })
        return id
    }

    fun isMobileDuplicate(mobile: String, excludeId: String? = null): Boolean {
        return _students.any { it.mobile == mobile && it.id != excludeId }
    }


}