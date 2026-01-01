package com.example.dailymate.ui.screens.addedittask

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dailymate.data.database.entity.Priority
import com.example.dailymate.data.database.entity.RepeatRule
import com.example.dailymate.ui.theme.*
import com.example.dailymate.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: String?,
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onShowSnackbar: (String, String?) -> Unit
) {
    val isEditMode = taskId != null
    val context = LocalContext.current
    
    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var dueDate by remember { mutableStateOf<Date?>(null) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var repeatOption by remember { mutableStateOf("None") }
    var selectedCategory by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Load task if in edit mode
    LaunchedEffect(taskId) {
        if (isEditMode) {
            viewModel.getTaskById(taskId!!).firstOrNull()?.let { task ->
                title = task.title
                description = task.description ?: ""
                selectedPriority = task.priority
                dueDate = task.dueAt?.let { Date(it) }
                reminderEnabled = task.reminderAt != null
                reminderTime = task.reminderAt
                repeatOption = when (task.repeat) {
                    RepeatRule.DAILY -> "Daily"
                    RepeatRule.WEEKLY -> "Weekly"
                    RepeatRule.MONTHLY -> "Monthly"
                    else -> "None"
                }
                selectedCategory = task.categoryId ?: ""
            }
        }
    }
    
    // Validation
    val isTitleValid = title.isNotBlank()
    val canSave = isTitleValid
    
    // Date formatters
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Task" else "Add Task",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // A. Title Field (Required)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Title *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Add Title") },
                        isError = title.isBlank() && title.isNotEmpty(),
                        supportingText = {
                            if (title.isBlank() && title.isNotEmpty()) {
                                Text("Title can't be empty", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = DividerBorder,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
                
                // B. Description Field
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Add details about this task...") },
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = DividerBorder,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
                
                // C. Priority Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PriorityChipSelector(
                            priority = Priority.LOW,
                            isSelected = selectedPriority == Priority.LOW,
                            onClick = { selectedPriority = Priority.LOW },
                            modifier = Modifier.weight(1f)
                        )
                        PriorityChipSelector(
                            priority = Priority.MEDIUM,
                            isSelected = selectedPriority == Priority.MEDIUM,
                            onClick = { selectedPriority = Priority.MEDIUM },
                            modifier = Modifier.weight(1f)
                        )
                        PriorityChipSelector(
                            priority = Priority.HIGH,
                            isSelected = selectedPriority == Priority.HIGH,
                            onClick = { selectedPriority = Priority.HIGH },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // D. Due Date
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val calendar = Calendar.getInstance()
                                dueDate?.let { calendar.time = it }
                                
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        calendar.set(year, month, dayOfMonth)
                                        dueDate = calendar.time
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DividerBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = if (dueDate != null) Primary else TextSecondary
                                )
                                Text(
                                    text = dueDate?.let { "Due: ${dateFormatter.format(it)}" }
                                        ?: "Set due date",
                                    color = if (dueDate != null) TextPrimary else TextSecondary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            if (dueDate != null) {
                                IconButton(onClick = { dueDate = null }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear date",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                
                // E. Reminder
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reminder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Surface,
                                checkedTrackColor = Primary
                            )
                        )
                    }
                    
                    if (reminderEnabled) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val calendar = Calendar.getInstance()
                                    reminderTime?.let { calendar.timeInMillis = it }
                                    
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            calendar.set(Calendar.MINUTE, minute)
                                            reminderTime = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DividerBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (reminderTime != null) Primary else TextSecondary
                                )
                                Text(
                                    text = reminderTime?.let { "Reminder time: ${timeFormatter.format(Date(it))}" }
                                        ?: "Set reminder time",
                                    color = if (reminderTime != null) TextPrimary else TextSecondary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        
                        // Repeat options
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DividerBorder)
                        ) {
                            var expanded by remember { mutableStateOf(false) }
                            
                            Box {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded = true }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Repeat,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                        Text(
                                            text = "Repeat: $repeatOption",
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    listOf("None", "Daily", "Weekly", "Monthly").forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                repeatOption = option
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // F. Category/List
                val categories by viewModel.categories.collectAsState()
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DividerBorder)
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        val currentCategoryName = categories.find { it.id == selectedCategory }?.name ?: "None"
                        
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Label,
                                        contentDescription = null,
                                        tint = Primary
                                    )
                                    Text(
                                        text = currentCategoryName,
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                            
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None") },
                                    onClick = {
                                        selectedCategory = ""
                                        expanded = false
                                    }
                                )
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            selectedCategory = category.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Bottom Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Save Button
                Button(
                    onClick = {
                        if (canSave) {
                            val repeatRule = when (repeatOption) {
                                "Daily" -> RepeatRule.DAILY
                                "Weekly" -> RepeatRule.WEEKLY
                                "Monthly" -> RepeatRule.MONTHLY
                                else -> RepeatRule.NONE
                            }
                            
                            viewModel.addTask(
                                title = title,
                                description = if (description.isBlank()) null else description,
                                priority = selectedPriority,
                                dueAt = dueDate?.time,
                                reminderAt = if (reminderEnabled) reminderTime else null,
                                repeat = repeatRule,
                                categoryId = selectedCategory
                            )
                            
                            onShowSnackbar(
                                if (isEditMode) "Task updated" else "Task saved",
                                null
                            )
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = canSave,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = DividerBorder
                    )
                ) {
                    Text(
                        "Save Task",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                
                // Cancel button
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun PriorityChipSelector(
    priority: Priority,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, borderColor, textColor, label) = when (priority) {
        Priority.HIGH -> if (isSelected) {
            Quadruple(PriorityHigh, PriorityHigh, Color.White, "High")
        } else {
            Quadruple(PriorityHighBg, PriorityHigh, PriorityHigh, "High")
        }
        Priority.MEDIUM -> if (isSelected) {
            Quadruple(PriorityMedium, PriorityMedium, Color.White, "Medium")
        } else {
            Quadruple(PriorityMediumBg, PriorityMedium, PriorityMedium, "Medium")
        }
        Priority.LOW -> if (isSelected) {
            Quadruple(PriorityLow, PriorityLow, Color.White, "Low")
        } else {
            Quadruple(PriorityLowBg, PriorityLow, PriorityLow, "Low")
        }
    }
    
    Card(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
