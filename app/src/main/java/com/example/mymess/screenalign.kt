package com.example.mymess

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mymess.navigation.Screen
import com.example.mymess.ui.auth.LoginScreen
import com.example.mymess.ui_for_admin.AdminDashboardScreen
import com.example.mymess.ui_for_admin.NameOfStudentScreen
import com.example.mymess.ui_for_user.UserHomeScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.mymess.ui_for_admin.*
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route != Screen.Login.route

    val adminNavItems = listOf(
        Screen.AdminDashboard,
        Screen.StudentList,
        Screen.Records,
        Screen.EmployeeList
    )
    
    val userNavItems = listOf(
        Screen.UserDashboard
    )

    val isUserRoute = currentDestination?.route == Screen.UserDashboard.route 

    val navItems = if (isUserRoute) userNavItems else adminNavItems

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { 
                                screen.icon?.let { 
                                    Icon(it, contentDescription = screen.title) 
                                } 
                            },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFFF5722),
                                selectedTextColor = Color(0xFFFF5722),
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(700)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(700)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(700)
                )
            }
        ) {
            composable(Screen.Login.route) {
                LoginScreen(navController)
            }
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(navController)
            }
            composable(Screen.UserDashboard.route) {
                UserHomeScreen(navController)
            }
            composable(Screen.StudentList.route) {
                NameOfStudentScreen(
                    onStudentClick = { studentId ->
                       navController.navigate(Screen.StudentProfile.createRoute(studentId))
                    }
                )
            }
            composable(
                route = Screen.StudentProfile.route,
                arguments = listOf(navArgument("studentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")
                if (studentId != null) {
                    StudentProfileScreen(studentId = studentId)
                }
            }
            composable(Screen.EmployeeList.route) {
                EmployeeListScreen(
                    onEmployeeClick = { employeeId ->
                        navController.navigate(Screen.EmployeeDetail.createRoute(employeeId))
                    }
                )
            }
            composable(
                route = Screen.EmployeeDetail.route,
                arguments = listOf(navArgument("employeeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val employeeId = backStackEntry.arguments?.getString("employeeId")
                if(employeeId != null) {
                    EmployeeProfileScreen(employeeId = employeeId)
                }
            }
             composable(Screen.Records.route) {
                RecordsScreen()
            }
            composable("user_dashboard") {
                UserHomeScreen(navController)
            }
        }
    }
}
