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
import androidx.compose.material.icons.filled.ArrowDropDown
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
    
    // Tasks observation - fetch based on current view
    // For month view, fetch month range. For other filters, fetch a wider range
    val taskDateRange = remember(currentMonth, selectedFilter) {
        when (selectedFilter) {
            CalendarFilter.THIS_WEEK, CalendarFilter.OVERDUE -> {
                // For week and overdue, fetch from 30 days ago to 30 days ahead
                val start = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -30)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val end = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 30)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                
                start to end
            }
            else -> {
                // For ALL, TODAY, THIS_MONTH - fetch current month
                val start = Calendar.getInstance().apply {
                    set(currentMonth.get(Calendar.YEAR), currentMonth.get(Calendar.MONTH), 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val end = Calendar.getInstance().apply {
                    set(currentMonth.get(Calendar.YEAR), currentMonth.get(Calendar.MONTH), 
                        currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                
                start to end
            }
        }
    }
    
    val tasks by viewModel.getTasksInRange(
        start = taskDateRange.first,
        end = taskDateRange.second
    ).collectAsState(initial = emptyList())

    val filteredTasks by remember(tasks, selectedDate, selectedFilter) {
        derivedStateOf {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            when (selectedFilter) {
                CalendarFilter.ALL -> tasks.filter { task ->
                    task.dueAt?.let { isSameDay(it, selectedDate.timeInMillis) } ?: false
                }
                CalendarFilter.TODAY -> tasks.filter { task ->
                    task.dueAt?.let { isSameDay(it, today.timeInMillis) } ?: false
                }
                CalendarFilter.THIS_WEEK -> tasks.filter { task ->
                    task.dueAt?.let { isInSameWeek(it, today.timeInMillis) } ?: false
                }
                CalendarFilter.THIS_MONTH -> tasks.filter { task ->
                    task.dueAt?.let { isInSameMonth(it, today.timeInMillis) } ?: false
                }
                CalendarFilter.OVERDUE -> tasks.filter { task ->
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Quick Filters
            item {
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
            }
            
            // Calendar Month View
            item {
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
                        // Month Header with Dropdowns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var showMonthDropdown by remember { mutableStateOf(false) }
                            var showYearDropdown by remember { mutableStateOf(false) }
                            
                            val currentMonthIndex = currentMonth.get(Calendar.MONTH)
                            val currentYear = currentMonth.get(Calendar.YEAR)
                            val months = listOf(
                                "January", "February", "March", "April", "May", "June",
                                "July", "August", "September", "October", "November", "December"
                            )
                            
                            // Month Dropdown
                            Box {
                                Card(
                                    modifier = Modifier.clickable { showMonthDropdown = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = months[currentMonthIndex],
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                DropdownMenu(
                                    expanded = showMonthDropdown,
                                    onDismissRequest = { showMonthDropdown = false },
                                    modifier = Modifier
                                        .background(Surface)
                                        .heightIn(max = 400.dp)
                                ) {
                                    months.forEachIndexed { index, month ->
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    month,
                                                    fontWeight = if (index == currentMonthIndex) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (index == currentMonthIndex) Primary else TextPrimary
                                                )
                                            },
                                            onClick = {
                                                currentMonth = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, currentYear)
                                                    set(Calendar.MONTH, index)
                                                    set(Calendar.DAY_OF_MONTH, 1)
                                                }
                                                showMonthDropdown = false
                                            },
                                            colors = MenuDefaults.itemColors(
                                                textColor = TextPrimary,
                                                leadingIconColor = TextPrimary,
                                                trailingIconColor = TextPrimary
                                            )
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Year Dropdown
                            Box {
                                Card(
                                    modifier = Modifier.clickable { showYearDropdown = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = currentYear.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                DropdownMenu(
                                    expanded = showYearDropdown,
                                    onDismissRequest = { showYearDropdown = false },
                                    modifier = Modifier
                                        .background(Surface)
                                        .heightIn(max = 300.dp)
                                ) {
                                    // Show years from 2020 to 2030
                                    (2020..2030).forEach { year ->
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    year.toString(),
                                                    fontWeight = if (year == currentYear) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (year == currentYear) Primary else TextPrimary
                                                )
                                            },
                                            onClick = {
                                                currentMonth = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, year)
                                                    set(Calendar.MONTH, currentMonthIndex)
                                                    set(Calendar.DAY_OF_MONTH, 1)
                                                }
                                                showYearDropdown = false
                                            },
                                            colors = MenuDefaults.itemColors(
                                                textColor = TextPrimary,
                                                leadingIconColor = TextPrimary,
                                                trailingIconColor = TextPrimary
                                            )
                                        )
                                    }
                                }
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
                            tasks = tasks,
                            onDateSelected = { date ->
                                selectedDate = date
                                selectedFilter = CalendarFilter.ALL
                            }
                        )
                    }
                }
            }
            
            // Task List Header
            item {
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
            }
            
            // Task List or Empty State
            if (filteredTasks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tasks for this selection",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                }
            } else {
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
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
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
    onCheckboxClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
