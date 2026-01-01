package com.example.dailymate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailymate.data.database.dao.TaskStats
import com.example.dailymate.data.database.entity.Priority
import com.example.dailymate.data.database.entity.RepeatRule
import com.example.dailymate.data.database.entity.TaskEntity
import com.example.dailymate.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    
    // Filter state
    private val _filterState = MutableStateFlow("ALL")
    val filterState: StateFlow<String> = _filterState.asStateFlow()
    
    // Sort state
    private val _sortState = MutableStateFlow("PRIORITY_DESC")
    val sortState: StateFlow<String> = _sortState.asStateFlow()
    
    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Observe tasks from repository
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<TaskEntity>> = combine(
        _sortState,
        _filterState,
        _searchQuery
    ) { sort, filter, query ->
        Triple(sort, filter, query)
    }.flatMapLatest { (sort, filter, query) ->
        repository.observeTasks(sort, filter, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Statistics
    val stats: StateFlow<TaskStats?> = repository.observeStats().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    
    val highPriorityNotDone: StateFlow<List<TaskEntity>> = repository.observeHighPriorityNotDone().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    
    val categories: StateFlow<List<com.example.dailymate.data.database.entity.CategoryEntity>> = 
        repository.observeCategories().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    fun getTaskById(taskId: String): Flow<TaskEntity?> = repository.observeTask(taskId)

    fun getTasksInRange(start: Long, end: Long): Flow<List<TaskEntity>> = 
        repository.observeTasksInRange(start, end)
        
    fun getOverdueTasks(): Flow<List<TaskEntity>> = 
        repository.observeOverdue(System.currentTimeMillis())

    fun addTask(
        title: String,
        description: String? = null,
        priority: Priority = Priority.MEDIUM,
        dueAt: Long? = null,
        reminderAt: Long? = null,
        repeat: RepeatRule = RepeatRule.NONE,
        categoryId: String? = null
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val task = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                priority = priority,
                isDone = false,
                dueAt = dueAt,
                reminderAt = reminderAt,
                repeat = repeat,
                categoryId = categoryId,
                isStarred = false,
                createdAt = now,
                updatedAt = now,
                doneAt = null
            )
            repository.upsertTask(task)
        }
    }

    fun toggleTaskDone(task: TaskEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val updatedTask = task.copy(
                isDone = !task.isDone,
                doneAt = if (!task.isDone) now else null,
                updatedAt = now
            )
            repository.upsertTask(updatedTask)
        }
    }

    fun toggleTaskStar(task: TaskEntity) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                isStarred = !task.isStarred,
                updatedAt = System.currentTimeMillis()
            )
            repository.upsertTask(updatedTask)
        }
    }

    fun updateSort(sort: String) {
        _sortState.value = sort
    }

    fun updateFilter(filter: String) {
        _filterState.value = filter
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }
    
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun addCategory(name: String, color: Int?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val category = com.example.dailymate.data.database.entity.CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                color = color,
                createdAt = now,
                updatedAt = now
            )
            repository.upsertCategory(category)
        }
    }

    fun updateCategory(category: com.example.dailymate.data.database.entity.CategoryEntity) {
        viewModelScope.launch {
            repository.upsertCategory(category.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteCategory(category: com.example.dailymate.data.database.entity.CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}
