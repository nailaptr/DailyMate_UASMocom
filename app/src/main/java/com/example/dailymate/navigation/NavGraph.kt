package com.example.dailymate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dailymate.ui.screens.addedittask.AddEditTaskScreen
import com.example.dailymate.ui.screens.calendar.CalendarScreen
import com.example.dailymate.ui.screens.lists.ListsScreen
import com.example.dailymate.ui.screens.mine.MineScreen
import com.example.dailymate.ui.screens.taskdetail.TaskDetailScreen
import com.example.dailymate.ui.screens.tasks.TasksScreen
import com.example.dailymate.viewmodel.TaskViewModel

@Composable
fun DailyMateNavGraph(
    navController: NavHostController,
    viewModel: TaskViewModel,
    onShowSnackbar: (String, String?) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tasks.route
    ) {
        // Tasks Screen (Home)
        composable(Screen.Tasks.route) {
            TasksScreen(
                viewModel = viewModel,
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onNavigateToAddTask = {
                    navController.navigate(Screen.AddEditTask.createRoute())
                },
                onShowSnackbar = onShowSnackbar
            )
        }
        
        // Calendar Screen
        composable(Screen.Calendar.route) {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onNavigateToAddTask = {
                    navController.navigate(Screen.AddEditTask.createRoute())
                },
                onShowSnackbar = onShowSnackbar
            )
        }
        
        // Mine Screen (Progress tracking)
        composable(Screen.Mine.route) {
            MineScreen(
                viewModel = viewModel,
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onShowSnackbar = onShowSnackbar
            )
        }
        
        // Lists/Categories Screen
        composable(Screen.Lists.route) {
            ListsScreen(
                viewModel = viewModel,
                onShowSnackbar = onShowSnackbar
            )
        }
        
        // Task Detail Screen
        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { 
                    navController.navigate(Screen.AddEditTask.createRoute(taskId))
                },
                onShowSnackbar = onShowSnackbar
            )
        }
        
        // Add/Edit Task Screen
        composable(
            route = Screen.AddEditTask.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            AddEditTaskScreen(
                taskId = taskId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onShowSnackbar = onShowSnackbar
            )
        }
    }
}
