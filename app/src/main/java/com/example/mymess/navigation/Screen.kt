package com.example.mymess.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    Login("login", "Login"),
    AdminDashboard("admin_dashboard", "Home", Icons.Default.Home),
    StudentList("student_list", "Students", Icons.Default.Person),
    StudentProfile("student_profile/{studentId}", "Student Profile"), 
    EmployeeList("employee_list", "Employees", Icons.Default.Work),
    EmployeeDetail("employee_detail/{employeeId}", "Employee Details"),
    UserDashboard("user_dashboard", "User Dashboard", Icons.Default.Home),
    Records("records", "Records", Icons.Default.DateRange);

    fun createRoute(vararg args: String): String {
        var res = route
        args.forEach { arg ->
            res = res.replaceFirst(Regex("\\{[^}]*\\}"), arg)
        }
        return res
    }
}
