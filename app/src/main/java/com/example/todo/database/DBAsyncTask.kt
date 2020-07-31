package com.example.todo.database

import android.content.Context
import android.os.AsyncTask
import androidx.room.Room

class DBAsyncTask(private val context: Context, private val taskEntity: TaskEntity, private val mode: Int) : AsyncTask<Void, Void, Boolean>() {

    private val db= Room.databaseBuilder(context, TaskDatabase::class.java, "todo_tasks-db").build()
    override fun doInBackground(vararg p0: Void?): Boolean {

        return when (mode) {

            2 -> {
                db.taskDao().insertTask(taskEntity)
                db.close()
                true
            }

            3 -> {
                db.taskDao().deleteTask(taskEntity)
                db.close()
                true
            }

            4 ->{
                db.taskDao().updateTask(taskEntity)
                db.close()
                true
            }

            else-> false
        }
    }
}