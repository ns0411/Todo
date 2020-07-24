package com.example.todo.database

import android.content.Context
import android.os.AsyncTask
import androidx.room.Room

class DBAsyncTask(val context: Context, val taskEntity: TaskEntity, val mode: Int) : AsyncTask<Void, Void, Boolean>() {

    val db= Room.databaseBuilder(context, TaskDatabase::class.java, "asks012-db").build()
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