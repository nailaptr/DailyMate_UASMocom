package com.example.dailymate.ui.screens.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dailymate.data.database.entity.CategoryEntity
import com.example.dailymate.ui.theme.*
import com.example.dailymate.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    viewModel: TaskViewModel,
    onShowSnackbar: (String, String?) -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var newCategoryName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Primary) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Lists", 
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
                onClick = {
                    newCategoryName = ""
                    selectedColor = Primary
                    showAddDialog = true
                },
                containerColor = Primary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Category",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = Background
    ) { paddingValues ->
        if (categories.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Label,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No categories yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap + to create your first category",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                items(categories, key = { it.id }) { category ->
                    CategoryCard(
                        category = category,
                        onEdit = {
                            selectedCategory = category
                            newCategoryName = category.name
                            selectedColor = category.color?.let { Color(it) } ?: Primary
                            showEditDialog = true
                        },
                        onDelete = {
                            selectedCategory = category
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // Add Category Dialog
    if (showAddDialog) {
        CategoryDialog(
            title = "Add Category",
            categoryName = newCategoryName,
            onCategoryNameChange = { newCategoryName = it },
            selectedColor = selectedColor,
            onColorSelected = { selectedColor = it },
            onDismiss = { showAddDialog = false },
            onConfirm = {
                if (newCategoryName.isNotBlank()) {
                    viewModel.addCategory(newCategoryName, selectedColor.toArgb())
                    showAddDialog = false
                    onShowSnackbar("Category created", null)
                }
            }
        )
    }
    
    // Edit Category Dialog
    if (showEditDialog && selectedCategory != null) {
        CategoryDialog(
            title = "Edit Category",
            categoryName = newCategoryName,
            onCategoryNameChange = { newCategoryName = it },
            selectedColor = selectedColor,
            onColorSelected = { selectedColor = it },
            onDismiss = { showEditDialog = false },
            onConfirm = {
                if (newCategoryName.isNotBlank()) {
                    val updated = selectedCategory!!.copy(
                        name = newCategoryName,
                        color = selectedColor.toArgb()
                    )
                    viewModel.updateCategory(updated)
                    showEditDialog = false
                    onShowSnackbar("Category updated", null)
                }
            }
        )
    }
    
    // Delete Confirmation Dialog (Currently just shows snackbar, deletion logic not yet in ViewModel for categories)
    if (showDeleteDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete category?") },
            text = { Text("Tasks in this category will be moved to 'Uncategorized'.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedCategory?.let { viewModel.deleteCategory(it) }
                        showDeleteDialog = false
                        onShowSnackbar("Category deleted", "Undo")
                    }
                ) {
                    Text("Delete", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategoryCard(
    category: CategoryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = category.color?.let { Color(it) } ?: Primary
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Label,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    // Task count would require a separate query or join
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Danger
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDialog(
    title: String,
    categoryName: String,
    onCategoryNameChange: (String) -> Unit,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = listOf(
        Primary, PriorityHigh, PriorityMedium, PriorityLow,
        Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF3F51B5),
        Color(0xFF00BCD4), Color(0xFF009688), Color(0xFFFF9800)
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = onCategoryNameChange,
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                
                // Color picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.take(5).forEach { color ->
                            ColorCircle(
                                color = color,
                                isSelected = color == selectedColor,
                                onClick = { onColorSelected(color) }
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.drop(5).forEach { color ->
                            ColorCircle(
                                color = color,
                                isSelected = color == selectedColor,
                                onClick = { onColorSelected(color) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = categoryName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.padding(4.dp)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Label,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
