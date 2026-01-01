package com.example.dailymate.ui.screens.mine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dailymate.data.database.dao.TaskStats
import com.example.dailymate.data.database.entity.TaskEntity
import com.example.dailymate.ui.screens.calendar.CompactTaskCard
import com.example.dailymate.ui.theme.*
import com.example.dailymate.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(
    viewModel: TaskViewModel,
    onNavigateToTaskDetail: (String) -> Unit,
    onShowSnackbar: (String, String?) -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val highPriorityNotDone by viewModel.highPriorityNotDone.collectAsState()
    
    // Extract statistics
    val totalTasks = stats?.total ?: 0
    val doneTasks = stats?.done ?: 0
    val notDoneTasks = stats?.notDone ?: 0
    val completionRate = if (totalTasks > 0) (doneTasks.toFloat() / totalTasks * 100).toInt() else 0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mine", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        if (totalTasks == 0) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()) // Ensure empty state is scrollable
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.TaskAlt,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No tasks yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start by adding your first task on Tasks tab.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Cards Grid
                item {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Total",
                            value = totalTasks.toString(),
                            icon = Icons.Default.List,
                            color = Primary,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Done",
                            value = doneTasks.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = PriorityLow,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Not Done",
                            value = notDoneTasks.toString(),
                            icon = Icons.Default.PriorityHigh,
                            color = PriorityMedium,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Completion",
                            value = "$completionRate%",
                            icon = Icons.Default.TaskAlt,
                            color = if (completionRate >= 70) PriorityLow else if (completionRate >= 40) PriorityMedium else PriorityHigh,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Progress Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Completion Rate",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$completionRate%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                            
                            LinearProgressIndicator(
                                progress = completionRate / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(999.dp)),
                                color = Primary,
                                trackColor = DividerBorder
                            )
                            
                            Text(
                                text = "$doneTasks/$totalTasks tasks completed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                // High Priority - Not Done Section
                if (highPriorityNotDone.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = PriorityHigh,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "High Priority – Not Done",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                    
                    items(highPriorityNotDone, key = { it.id }) { task ->
                        CompactTaskCard(
                            task = task,
                            onTaskClick = { onNavigateToTaskDetail(task.id) },
                            onCheckboxClick = {
                                viewModel.toggleTaskDone(task)
                                onShowSnackbar("Task marked as done", "Undo")
                            }
                        )
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PriorityLowBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PriorityLow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Great job!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PriorityLow
                                    )
                                    Text(
                                        text = "No high priority tasks pending",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
