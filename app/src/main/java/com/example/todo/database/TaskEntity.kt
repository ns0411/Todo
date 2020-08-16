package com.example.todo.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @ColumnInfo(name="content") var taskContent: String,
    @ColumnInfo(name="date") var date:String,
    @ColumnInfo(name="time") var timeReminder:String,
    @ColumnInfo(name= "checked") var isCompleted : Boolean,
    @PrimaryKey(autoGenerate = true) var taskId:Int,
    @ColumnInfo(name="idForReminder") val alarmId:Int
)
