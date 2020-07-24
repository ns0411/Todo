package com.example.todo.database

import android.content.Context
import androidx.room.*

@Dao
interface TaskDao {

    @Insert
    fun insertTask(taskEntity: TaskEntity)

    @Delete
    fun deleteTask(taskEntity: TaskEntity)

    @Query("SELECT * FROM tasks")
    fun getAllTasks():List<TaskEntity>

    @Update
    fun updateTask(taskEntity: TaskEntity)

    companion object {
    @Volatile
    private var INSTANCE: RoomDatabase? = null

        fun getDatabase(context: Context): RoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomDatabase::class.java,
                    "todo_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }

}