package com.example.dailymate.data.database.dao

import androidx.room.*
import com.example.dailymate.data.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

// Data class for statistics
data class TaskStats(
    val total: Int,
    val done: Int,
    val notDone: Int,
    val highNotDone: Int,
    val medNotDone: Int,
    val lowNotDone: Int
)

@Dao
interface TaskDao {
    
    // ========== TASKS LIST QUERIES (with filter + search + sorting) ==========
    
    /**
     * Priority High to Low (default sorting):
     * - Not Done first
     * - Within Not Done: HIGH → MEDIUM → LOW
     * - Done tasks at bottom
     */
    @Query("""
        SELECT * FROM tasks
        WHERE
            (:status = 'ALL' OR (:status = 'DONE' AND isDone = 1) OR (:status = 'NOT_DONE' AND isDone = 0))
            AND (:q IS NULL OR :q = '' OR title LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%')
        ORDER BY
            isDone ASC,
            CASE priority
                WHEN 'HIGH' THEN 0
                WHEN 'MEDIUM' THEN 1
                ELSE 2
            END ASC,
            COALESCE(dueAt, 9223372036854775807) ASC,
            updatedAt DESC
    """)
    fun observeTasksPriorityDesc(status: String, q: String?): Flow<List<TaskEntity>>
    
    /**
     * Priority Low to High sorting:
     * - Not Done first
     * - Within Not Done: LOW → MEDIUM → HIGH
     * - Done tasks at bottom
     */
    @Query("""
        SELECT * FROM tasks
        WHERE
            (:status = 'ALL' OR (:status = 'DONE' AND isDone = 1) OR (:status = 'NOT_DONE' AND isDone = 0))
            AND (:q IS NULL OR :q = '' OR title LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%')
        ORDER BY
            isDone ASC,
            CASE priority
                WHEN 'LOW' THEN 0
                WHEN 'MEDIUM' THEN 1
                ELSE 2
            END ASC,
            COALESCE(dueAt, 9223372036854775807) ASC,
            updatedAt DESC
    """)
    fun observeTasksPriorityAsc(status: String, q: String?): Flow<List<TaskEntity>>
    
    /**
     * Due Date Nearest First sorting:
     * - Not Done first
     * - Sorted by due date (nearest first)
     * - Done tasks at bottom
     */
    @Query("""
        SELECT * FROM tasks
        WHERE
            (:status = 'ALL' OR (:status = 'DONE' AND isDone = 1) OR (:status = 'NOT_DONE' AND isDone = 0))
            AND (:q IS NULL OR :q = '' OR title LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%')
        ORDER BY
            isDone ASC,
            COALESCE(dueAt, 9223372036854775807) ASC,
            CASE priority
                WHEN 'HIGH' THEN 0
                WHEN 'MEDIUM' THEN 1
                ELSE 2
            END ASC,
            updatedAt DESC
    """)
    fun observeTasksDueNearest(status: String, q: String?): Flow<List<TaskEntity>>
    
    // ========== CALENDAR QUERIES ==========
    
    /**
     * Get tasks within a date range (for Calendar view)
     */
    @Query("""
        SELECT * FROM tasks
        WHERE dueAt IS NOT NULL AND dueAt BETWEEN :startInclusive AND :endInclusive
        ORDER BY
            isDone ASC,
            CASE priority
                WHEN 'HIGH' THEN 0
                WHEN 'MEDIUM' THEN 1
                ELSE 2
            END ASC,
            COALESCE(dueAt, 9223372036854775807) ASC
    """)
    fun observeTasksInRange(startInclusive: Long, endInclusive: Long): Flow<List<TaskEntity>>
    
    /**
     * Get overdue tasks (not done and past due date)
     */
    @Query("""
        SELECT * FROM tasks
        WHERE isDone = 0 AND dueAt IS NOT NULL AND dueAt < :now
        ORDER BY dueAt ASC
    """)
    fun observeOverdue(now: Long): Flow<List<TaskEntity>>
    
    // ========== MINE / PROGRESS QUERIES ==========
    
    /**
     * Get aggregated statistics for Mine screen
     */
    @Query("""
        SELECT
            COUNT(*) AS total,
            SUM(CASE WHEN isDone = 1 THEN 1 ELSE 0 END) AS done,
            SUM(CASE WHEN isDone = 0 THEN 1 ELSE 0 END) AS notDone,
            SUM(CASE WHEN isDone = 0 AND priority = 'HIGH' THEN 1 ELSE 0 END) AS highNotDone,
            SUM(CASE WHEN isDone = 0 AND priority = 'MEDIUM' THEN 1 ELSE 0 END) AS medNotDone,
            SUM(CASE WHEN isDone = 0 AND priority = 'LOW' THEN 1 ELSE 0 END) AS lowNotDone
        FROM tasks
    """)
    fun observeStats(): Flow<TaskStats>
    
    /**
     * Get high priority tasks that are not done
     */
    @Query("""
        SELECT * FROM tasks
        WHERE isDone = 0 AND priority = 'HIGH'
        ORDER BY COALESCE(dueAt, 9223372036854775807) ASC, updatedAt DESC
        LIMIT :limit
    """)
    fun observeHighPriorityNotDone(limit: Int = 20): Flow<List<TaskEntity>>
    
    // ========== BASIC CRUD OPERATIONS ==========
    
    /**
     * Get task by ID
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeTaskById(taskId: String): Flow<TaskEntity?>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?
    
    /**
     * Get all tasks (for general use)
     */
    @Query("SELECT * FROM tasks ORDER BY updatedAt DESC")
    fun observeAllTasks(): Flow<List<TaskEntity>>
    
    /**
     * Get tasks with active reminders (for rescheduling after reboot)
     */
    @Query("""
        SELECT * FROM tasks
        WHERE reminderAt IS NOT NULL AND isDone = 0
        ORDER BY reminderAt ASC
    """)
    suspend fun getTasksWithActiveReminders(): List<TaskEntity>
    
    /**
     * Insert or update task
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)
    
    /**
     * Insert multiple tasks
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>)
    
    /**
     * Update task
     */
    @Update
    suspend fun update(task: TaskEntity)
    
    /**
     * Delete task
     */
    @Delete
    suspend fun delete(task: TaskEntity)
    
    /**
     * Delete task by ID
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: String)
    
    /**
     * Delete all tasks
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
