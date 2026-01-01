package com.example.dailymate.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dailymate.data.database.entity.Priority
import com.example.dailymate.data.database.entity.TaskEntity
import com.example.dailymate.ui.theme.*
import com.example.dailymate.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TaskViewModel,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToAddTask: () -> Unit,
    onShowSnackbar: (String, String?) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val sortState by viewModel.sortState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Background)) {
                TopAppBar(
                    title = { 
                        Text(
                            "Tasks", 
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        ) 
                    },
                    actions = {
                        IconButton(onClick = { showSearchBar = !showSearchBar }) {
                            Icon(
                                Icons.Default.Search, 
                                contentDescription = "Search",
                                tint = TextPrimary // Darker icon for visibility
                            )
                        }
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(
                                Icons.Default.Sort, 
                                contentDescription = "Sort",
                                tint = TextPrimary // Darker icon for visibility
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Background,
                        titleContentColor = TextPrimary,
                        actionIconContentColor = TextPrimary
                    )
                )
                
                if (showSearchBar) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.updateSearch(it) },
                        onClearQuery = { viewModel.updateSearch("") }
                    )
                }
                
                // Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterState == "ALL",
                            onClick = { viewModel.updateFilter("ALL") },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState == "NOT_DONE",
                            onClick = { viewModel.updateFilter("NOT_DONE") },
                            label = { Text("Not Done") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterState == "DONE",
                            onClick = { viewModel.updateFilter("DONE") },
                            label = { Text("Done") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                containerColor = Primary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(28.dp) // Larger icon for visibility
                )
            }
        },
        containerColor = Background
    ) { paddingValues ->
        if (tasks.isEmpty() && searchQuery.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()) // Make empty state scrollable too
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp), // Extra bottom padding for FAB
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onTaskClick = { onNavigateToTaskDetail(task.id) },
                        onCheckboxClick = {
                            viewModel.toggleTaskDone(task)
                            onShowSnackbar(
                                if (!task.isDone) "Task completed" else "Task marked as not done",
                                "Undo"
                            )
                        }
                    )
                }
            }
        }
    }
    
    // Sort Dialog
    if (showSortDialog) {
        SortDialog(
            currentSort = sortState,
            onSortSelected = { viewModel.updateSort(it) },
            onDismiss = { showSortDialog = false }
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search tasks...") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Text("×", style = MaterialTheme.typography.titleLarge)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(999.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun TaskCard(
    task: TaskEntity,
    onTaskClick: () -> Unit,
    onCheckboxClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (task.isDone) 0.7f else 1f)
            .clickable(onClick = onTaskClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onCheckboxClick() },
                colors = CheckboxDefaults.colors(checkedColor = Primary)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isDone) DoneText else TextPrimary,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!task.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Priority chip and status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriorityChip(priority = task.priority)
                    
                    Text(
                        text = if (task.isDone) "Done" else "Not Done",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.isDone) DoneText else TextSecondary
                    )
                    
                    if (task.dueAt != null) {
                        Text(
                            text = "• ${formatDate(task.dueAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            // Star icon
            Icon(
                imageVector = if (task.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (task.isStarred) PriorityMedium else TextSecondary,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun PriorityChip(priority: Priority) {
    val (backgroundColor, textColor, label) = when (priority) {
        Priority.HIGH -> Triple(PriorityHighBg, PriorityHigh, "High")
        Priority.MEDIUM -> Triple(PriorityMediumBg, PriorityMedium, "Medium")
        Priority.LOW -> Triple(PriorityLowBg, PriorityLow, "Low")
    }
    
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = backgroundColor,
        modifier = Modifier.height(28.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No tasks yet",
            style = MaterialTheme.typography.titleLarge,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to add your first task",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun SortDialog(
    currentSort: String,
    onSortSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort by") },
        text = {
            Column {
                SortOption(
                    label = "Priority (High → Low)",
                    isSelected = currentSort == "PRIORITY_DESC",
                    onClick = {
                        onSortSelected("PRIORITY_DESC")
                        onDismiss()
                    }
                )
                SortOption(
                    label = "Priority (Low → High)",
                    isSelected = currentSort == "PRIORITY_ASC",
                    onClick = {
                        onSortSelected("PRIORITY_ASC")
                        onDismiss()
                    }
                )
                SortOption(
                    label = "Due date (Nearest first)",
                    isSelected = currentSort == "DUE_NEAREST",
                    onClick = {
                        onSortSelected("DUE_NEAREST")
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SortOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

// Helper functions
fun formatDate(millis: Long): String {
    val date = Date(millis)
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    return sdf.format(date)
}
