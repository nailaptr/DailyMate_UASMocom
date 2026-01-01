package com.example.dailymate.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: Int?,
    val createdAt: Long,
    val updatedAt: Long
)
