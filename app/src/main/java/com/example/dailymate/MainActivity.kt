package com.example.dailymate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dailymate.data.database.DailyMateDatabase
import com.example.dailymate.data.repository.TaskRepository
import com.example.dailymate.navigation.DailyMateNavGraph
import com.example.dailymate.navigation.Screen
import com.example.dailymate.navigation.bottomNavItems
import com.example.dailymate.ui.theme.DailyMateTheme
import com.example.dailymate.ui.theme.Primary
import com.example.dailymate.ui.theme.Surface
import com.example.dailymate.ui.theme.TextPrimary
import com.example.dailymate.ui.theme.TextSecondary
import com.example.dailymate.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = DailyMateDatabase.getDatabase(this)
        val repository = TaskRepository(database.taskDao(), database.categoryDao(), database.subtaskDao())
        
        setContent {
            DailyMateTheme {
                DailyMateApp(repository)
            }
        }
    }
}

@Composable
fun DailyMateApp(repository: TaskRepository) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Create ViewModel manually for simplicity in this UAS project
    val viewModel: TaskViewModel = remember { TaskViewModel(repository) }
    
    // Track current route to show/hide bottom nav
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Show bottom nav only on main screens (Tasks, Calendar, Mine, Lists)
    val showBottomNav = currentDestination?.route in bottomNavItems.map { it.route }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomNav) {
                DailyMateBottomNavigation(
                    currentDestination = currentDestination?.route,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            DailyMateNavGraph(
                navController = navController,
                viewModel = viewModel,
                onShowSnackbar = { message, actionLabel ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = message,
                            actionLabel = actionLabel,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun DailyMateBottomNavigation(
    currentDestination: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Surface,
        contentColor = TextPrimary,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = (currentDestination == screen.route)
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { 
                    Text(
                        screen.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ) 
                },
                selected = isSelected,
                onClick = { onNavigate(screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}