package com.example.dailymate.data.model

import java.util.Date
import java.util.UUID

// Priority enum matching blueprint specification
enum class Priority {
    LOW, MEDIUM, HIGH
}

// Task data model with all required fields from blueprint
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val isDone: Boolean = false,
    val dueDate: Date? = null,
    val reminderTime: Date? = null,
    val repeatRule: String? = null,
    val categoryId: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isStarred: Boolean = false
)
