package com.example.todo.database

import android.text.Editable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @ColumnInfo(name="content") var taskContent: String,
    @PrimaryKey(autoGenerate = true) var taskId:Int=0
)
