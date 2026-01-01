package com.example.dailymate.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dailymate.data.database.dao.CategoryDao
import com.example.dailymate.data.database.dao.SubtaskDao
import com.example.dailymate.data.database.dao.TaskDao
import com.example.dailymate.data.database.entity.CategoryEntity
import com.example.dailymate.data.database.entity.SubtaskEntity
import com.example.dailymate.data.database.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, CategoryEntity::class, SubtaskEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DailyMateDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subtaskDao(): SubtaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: DailyMateDatabase? = null
        
        fun getDatabase(context: Context): DailyMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DailyMateDatabase::class.java,
                    "dailymate_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
