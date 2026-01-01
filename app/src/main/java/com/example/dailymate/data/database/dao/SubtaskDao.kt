package com.example.dailymate.data.database.dao

import androidx.room.*
import com.example.dailymate.data.database.entity.SubtaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {
    
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY position ASC")
    fun observeSubtasksByTaskId(taskId: String): Flow<List<SubtaskEntity>>
    
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY position ASC")
    suspend fun getSubtasksByTaskId(taskId: String): List<SubtaskEntity>
    
    @Query("SELECT * FROM subtasks WHERE id = :subtaskId")
    suspend fun getSubtaskById(subtaskId: String): SubtaskEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(subtask: SubtaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(subtasks: List<SubtaskEntity>)
    
    @Update
    suspend fun update(subtask: SubtaskEntity)
    
    @Delete
    suspend fun delete(subtask: SubtaskEntity)
    
    @Query("DELETE FROM subtasks WHERE id = :subtaskId")
    suspend fun deleteById(subtaskId: String)
    
    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: String)
    
    @Query("DELETE FROM subtasks")
    suspend fun deleteAll()
}
