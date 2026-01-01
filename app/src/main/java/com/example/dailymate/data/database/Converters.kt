package com.example.dailymate.data.database

import androidx.room.TypeConverter
import com.example.dailymate.data.database.entity.Priority
import com.example.dailymate.data.database.entity.RepeatRule

class Converters {
    
    @TypeConverter
    fun fromPriority(value: Priority): String {
        return value.name
    }
    
    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }
    
    @TypeConverter
    fun fromRepeatRule(value: RepeatRule): String {
        return value.name
    }
    
    @TypeConverter
    fun toRepeatRule(value: String): RepeatRule {
        return RepeatRule.valueOf(value)
    }
}
