package com.example.dailymate.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dailymate.data.database.entity.TaskEntity
import com.example.dailymate.ui.screens.tasks.PriorityChip
import com.example.dailymate.ui.theme.*
import com.example.dailymate.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class CalendarFilter {
    ALL, TODAY, THIS_WEEK, THIS_MONTH, OVERDUE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: TaskViewModel,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToAddTask: () -> Unit,
    onShowSnackbar: (String, String?) -> Unit
) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedFilter by remember { mutableStateOf(CalendarFilter.ALL) }
    
    // Tasks observation
    val tasksByMonth by viewModel.getTasksInRange(
        start = Calendar.getInstance().apply {
            set(currentMonth.get(Calendar.YEAR), currentMonth.get(Calendar.MONTH), 1, 0, 0, 0)
        }.timeInMillis,
        end = Calendar.getInstance().apply {
            set(currentMonth.get(Calendar.YEAR), currentMonth.get(Calendar.MONTH), currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        }.timeInMillis
    ).collectAsState(initial = emptyList())

    val filteredTasks by remember(tasksByMonth, selectedDate, selectedFilter) {
        derivedStateOf {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            when (selectedFilter) {
                CalendarFilter.ALL -> tasksByMonth.filter { task ->
                    task.dueAt?.let { isSameDay(it, selectedDate.timeInMillis) } ?: false
                }
                CalendarFilter.TODAY -> tasksByMonth.filter { task ->
                    task.dueAt?.let { isSameDay(it, today.timeInMillis) } ?: false
                }
                CalendarFilter.THIS_WEEK -> tasksByMonth.filter { task ->
                    task.dueAt?.let { isInSameWeek(it, today.timeInMillis) } ?: false
                }
                CalendarFilter.THIS_MONTH -> tasksByMonth.filter { task ->
                    task.dueAt?.let { isInSameMonth(it, today.timeInMillis) } ?: false
                }
                CalendarFilter.OVERDUE -> tasksByMonth.filter { task ->
                    task.dueAt?.let { it < today.timeInMillis && !task.isDone } ?: false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Calendar", 
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
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Enable scrolling for all content
        ) {
            // Quick Filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == CalendarFilter.TODAY,
                        onClick = { selectedFilter = CalendarFilter.TODAY },
                        label = { Text("Today") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CalendarFilter.THIS_WEEK,
                        onClick = { selectedFilter = CalendarFilter.THIS_WEEK },
                        label = { Text("This Week") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CalendarFilter.THIS_MONTH,
                        onClick = { selectedFilter = CalendarFilter.THIS_MONTH },
                        label = { Text("This Month") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CalendarFilter.OVERDUE,
                        onClick = { selectedFilter = CalendarFilter.OVERDUE },
                        label = { Text("Overdue") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PriorityHighBg,
                            selectedLabelColor = PriorityHigh
                        )
                    )
                }
            }
            
            // Calendar Month View
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Month Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            currentMonth.add(Calendar.MONTH, -1)
                            currentMonth = currentMonth.clone() as Calendar
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                        }
                        
                        Text(
                            text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = {
                            currentMonth.add(Calendar.MONTH, 1)
                            currentMonth = currentMonth.clone() as Calendar
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Day names
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Calendar grid
                    CalendarGrid(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        tasks = tasksByMonth,
                        onDateSelected = { date ->
                            selectedDate = date
                            selectedFilter = CalendarFilter.ALL
                        }
                    )
                }
            }
            
            // Task List for Selected Date
            Text(
                text = if (selectedFilter == CalendarFilter.ALL) {
                    "Tasks for ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate.time)}"
                } else {
                    when (selectedFilter) {
                        CalendarFilter.TODAY -> "Today's Tasks"
                        CalendarFilter.THIS_WEEK -> "This Week's Tasks"
                        CalendarFilter.THIS_MONTH -> "This Month's Tasks"
                        CalendarFilter.OVERDUE -> "Overdue Tasks"
                        else -> "Tasks"
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            if (filteredTasks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No tasks for this date",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        CompactTaskCard(
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
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    selectedDate: Calendar,
    tasks: List<TaskEntity>,
    onDateSelected: (Calendar) -> Unit
) {
    val month = currentMonth.get(Calendar.MONTH)
    val year = currentMonth.get(Calendar.YEAR)
    
    // Get first day of month
    val firstDay = Calendar.getInstance().apply {
        set(year, month, 1)
    }
    val firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK) - 1
    
    // Get days in month
    val daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // Create grid
    val weeks = mutableListOf<List<Int?>>()
    var currentWeek = MutableList<Int?>(7) { null }
    
    // Fill first week
    for (i in 0 until firstDayOfWeek) {
        currentWeek[i] = null
    }
    
    var dayCounter = 1
    for (i in firstDayOfWeek until 7) {
        currentWeek[i] = dayCounter++
    }
    weeks.add(currentWeek.toList())
    
    // Fill remaining weeks
    while (dayCounter <= daysInMonth) {
        currentWeek = MutableList(7) { null }
        for (i in 0 until 7) {
            if (dayCounter <= daysInMonth) {
                currentWeek[i] = dayCounter++
            }
        }
        weeks.add(currentWeek.toList())
    }
    
    Column {
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    if (day != null) {
                        val date = Calendar.getInstance().apply {
                            set(year, month, day)
                        }
                        val isSelected = isSameDay(selectedDate.timeInMillis, date.timeInMillis)
                        val isToday = isSameDay(System.currentTimeMillis(), date.timeInMillis)
                        val hasTasks = tasks.any { task ->
                            task.dueAt?.let { isSameDay(it, date.timeInMillis) } ?: false
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> Primary
                                        isToday -> PriorityLowBg
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        isSelected -> Color.White
                                        isToday -> PriorityLow
                                        else -> TextPrimary
                                    },
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasTasks && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(Primary)
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CompactTaskCard(
    task: TaskEntity,
    onTaskClick: () -> Unit,
    onCheckboxClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTaskClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onCheckboxClick() },
                colors = CheckboxDefaults.colors(checkedColor = Primary)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isDone) DoneText else TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            PriorityChip(priority = task.priority)
        }
    }
}

// Helper functions (updated to use Long for performance)
fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isInSameWeek(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
}

fun isInSameMonth(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}
