package com.example.dailymate.data.repository

import com.example.dailymate.data.database.dao.CategoryDao
import com.example.dailymate.data.database.dao.SubtaskDao
import com.example.dailymate.data.database.dao.TaskDao
import com.example.dailymate.data.database.dao.TaskStats
import com.example.dailymate.data.database.entity.CategoryEntity
import com.example.dailymate.data.database.entity.SubtaskEntity
import com.example.dailymate.data.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao,
    private val subtaskDao: SubtaskDao
) {
    // Tasks
    fun observeTasks(sortOption: String, status: String, query: String?): Flow<List<TaskEntity>> {
        return when (sortOption) {
            "PRIORITY_DESC" -> taskDao.observeTasksPriorityDesc(status, query)
            "PRIORITY_ASC" -> taskDao.observeTasksPriorityAsc(status, query)
            else -> taskDao.observeTasksDueNearest(status, query)
        }
    }
    
    fun observeTask(taskId: String): Flow<TaskEntity?> = taskDao.observeTaskById(taskId)
    
    suspend fun upsertTask(task: TaskEntity) = taskDao.upsert(task)
    
    suspend fun deleteTask(taskId: String) = taskDao.deleteById(taskId)
    
    // Stats
    fun observeStats(): Flow<TaskStats> = taskDao.observeStats()
    
    fun observeHighPriorityNotDone(limit: Int = 20): Flow<List<TaskEntity>> = 
        taskDao.observeHighPriorityNotDone(limit)
        
    // Calendar
    fun observeTasksInRange(start: Long, end: Long): Flow<List<TaskEntity>> = 
        taskDao.observeTasksInRange(start, end)
        
    fun observeOverdue(now: Long): Flow<List<TaskEntity>> = taskDao.observeOverdue(now)
    
    // Categories
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAllCategories()
    
    suspend fun upsertCategory(category: CategoryEntity) = categoryDao.upsert(category)
    
    // Subtasks
    fun observeSubtasks(taskId: String): Flow<List<SubtaskEntity>> = 
        subtaskDao.observeSubtasksByTaskId(taskId)
        
    suspend fun upsertSubtasks(subtasks: List<SubtaskEntity>) = subtaskDao.upsertAll(subtasks)
}
