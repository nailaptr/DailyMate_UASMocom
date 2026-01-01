package com.example.dailymate.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

// Navigation routes for the app
sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Tasks : Screen(
        route = "tasks",
        title = "Tasks",
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    )
    
    object Calendar : Screen(
        route = "calendar",
        title = "Calendar",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )
    
    object Mine : Screen(
        route = "mine",
        title = "Mine",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
    
    object Lists : Screen(
        route = "lists",
        title = "Lists",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List
    )
    
    object TaskDetail : Screen(
        route = "task_detail/{taskId}",
        title = "Task Detail",
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    ) {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    
    object AddEditTask : Screen(
        route = "add_edit_task?taskId={taskId}",
        title = "Add Task",
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    ) {
        fun createRoute(taskId: String? = null) = if (taskId != null) {
            "add_edit_task?taskId=$taskId"
        } else {
            "add_edit_task"
        }
    }
}

// Bottom navigation items (4 tabs as per blueprint)
val bottomNavItems = listOf(
    Screen.Tasks,
    Screen.Calendar,
    Screen.Mine,
    Screen.Lists
)
