package com.example.mymess.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.mymess.data.StudentRepository
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun exportDailyReport(context: Context, date: Calendar) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
        val fileName = "Mess_Report_Daily_$dateStr.csv"
        
        val csvHeader = "Student Name,ID,Message/Status,Breakfast,Lunch,Dinner,Time\n"
        val stringBuilder = StringBuilder()
        stringBuilder.append(csvHeader)

        // Get logs for the specific date
        val logs = StudentRepository.getAttendanceForDate(date.timeInMillis)
        
        if (logs.isEmpty()) {
            Toast.makeText(context, "No records for $dateStr", Toast.LENGTH_SHORT).show()
            return
        }

        logs.forEach { log ->
            val student = StudentRepository.getStudent(log.studentId)
            val studentName = student?.name ?: "Unknown"
            val studentId = log.studentId
            val mealType = log.mealType
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
            
            // Simplified row for daily log: Name, ID, MealType, Time
            stringBuilder.append("$studentName,$studentId,$mealType,-,-,-,$time\n")
        }

        saveCsv(context, fileName, stringBuilder.toString())
    }

    fun exportMonthlyReport(context: Context, date: Calendar) {
        val monthStr = SimpleDateFormat("MMM_yyyy", Locale.getDefault()).format(date.time)
        val fileName = "Mess_Report_Monthly_$monthStr.csv"

        val csvHeader = "Student Name,ID,Remaining B,Remaining L,Remaining D,Breakfast Count,Lunch Count,Dinner Count,Total Meals Taken\n"
        val stringBuilder = StringBuilder()
        stringBuilder.append(csvHeader)

        val students = StudentRepository.students

        students.forEach { student ->
            // Note: Ideally we should filter counts by month, but current Repository stores total counts.
            // For now, we export current snapshot as per existing data model limits.
            
            val totalMeals = student.breakfastCount + student.lunchCount + student.dinnerCount
            
            stringBuilder.append("${student.name},${student.id},${student.remainingBreakfasts},${student.remainingLunches},${student.remainingDinners},${student.breakfastCount},${student.lunchCount},${student.dinnerCount},$totalMeals\n")
        }

        saveCsv(context, fileName, stringBuilder.toString())
    }

    fun exportMasterReport(context: Context, date: Calendar) {
        val monthStr = SimpleDateFormat("MMM_yyyy", Locale.getDefault()).format(date.time)
        val fileName = "Mess_Master_Report_$monthStr.csv"

        val stringBuilder = StringBuilder()

        // --- SECTION 1: STUDENTS ---
        stringBuilder.append("=== STUDENT REPORT ===\n")
        stringBuilder.append("Student Name,Meals Left (B),Meals Left (L),Meals Left (D),Sun Specials,Payments This Month\n")
        
        val students = StudentRepository.students
        
        // Let's filter payments for this specific month for the report
        val reportMonth = date.get(Calendar.MONTH)
        val reportYear = date.get(Calendar.YEAR)

        students.forEach { student ->
            // Calculate total payments this month for this student
            val payments = StudentRepository.getPaymentHistory(student.id)
            var amountPaidThisMonth = 0
            
            payments.forEach { payment ->
                val pDate = Calendar.getInstance().apply { timeInMillis = payment.date }
                if (pDate.get(Calendar.MONTH) == reportMonth && pDate.get(Calendar.YEAR) == reportYear) {
                    amountPaidThisMonth += payment.amount
                }
            }

            stringBuilder.append("${student.name},${student.remainingBreakfasts},${student.remainingLunches},${student.remainingDinners},${student.remainingSundayMeals},Rs. $amountPaidThisMonth\n")
        }

        stringBuilder.append("\n\n") // Blank lines to separate sections
        
        // --- SECTION 2: EMPLOYEES ---
        stringBuilder.append("=== EMPLOYEE REPORT ===\n")
        stringBuilder.append("Employee Name,Monthly Salary,Total Advance Paid,Payment History\n")
        
        val employees = StudentRepository.employees
        val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())

        employees.forEach { employee ->
            val advances = StudentRepository.getEmployeeAdvances(employee.id)
            var totalAdvancePaidThisMonth = 0.0
            val historyBuilder = StringBuilder()

            advances.forEach { payment ->
                 val pDate = Calendar.getInstance().apply { timeInMillis = payment.date }
                 if (pDate.get(Calendar.MONTH) == reportMonth && pDate.get(Calendar.YEAR) == reportYear) {
                     if (!payment.isSalaryPayment) {
                         totalAdvancePaidThisMonth += payment.amount
                     }
                     val type = if (payment.isSalaryPayment) "[Salary]" else "[Advance]"
                     historyBuilder.append("$type Rs.${payment.amount} on ${formatter.format(Date(payment.date))} | ")
                 }
            }
            
            val historyStr = if (historyBuilder.isNotEmpty()) historyBuilder.toString().dropLast(3) else "No payments this month"
            
            // Note: Wrapping history in quotes handles commas inside the string for CSV format
            stringBuilder.append("${employee.name},Rs. ${employee.monthlySalary},Rs. $totalAdvancePaidThisMonth,\"$historyStr\"\n")
        }

        saveCsv(context, fileName, stringBuilder.toString())
    }

    private fun saveCsv(context: Context, fileName: String, content: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    val outputStream: OutputStream? = resolver.openOutputStream(uri)
                    outputStream?.use {
                        it.write(content.toByteArray())
                    }
                    Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                } else {
                     Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Legacy
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use {
                    it.write(content.toByteArray())
                }
                 Toast.makeText(context, "Saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
             Toast.makeText(context, "Error saving CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
