package com.example.todo.database

import androidx.room.*

@Dao
interface TaskDao {

    @Insert
    fun insertTask(taskEntity: TaskEntity)

    @Delete
    fun deleteTask(taskEntity: TaskEntity)

    @Update
    fun updateTask(taskEntity: TaskEntity)

    @Query("SELECT * FROM tasks")
    fun getAllTasks():List<TaskEntity>

}