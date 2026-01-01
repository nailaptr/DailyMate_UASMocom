package com.example.dailymate.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Priority { LOW, MEDIUM, HIGH }
enum class RepeatRule { NONE, DAILY, WEEKLY, MONTHLY }

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["isDone", "priority", "dueAt"]),
        Index(value = ["categoryId"])
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val priority: Priority,
    val isDone: Boolean,
    val dueAt: Long?,  // epoch millis
    val reminderAt: Long?,  // epoch millis
    val repeat: RepeatRule,
    val categoryId: String?,
    val isStarred: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val doneAt: Long?  // when task was completed
)
