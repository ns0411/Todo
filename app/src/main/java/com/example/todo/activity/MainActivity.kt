package com.example.todo.activity

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.room.Room
import com.example.todo.R
import com.example.todo.adapter.TaskAdapter
import com.example.todo.database.DBAsyncTask
import com.example.todo.database.TaskDatabase
import com.example.todo.database.TaskEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.add_new_task_dialog.view.*


class MainActivity : AppCompatActivity() {

    lateinit var floatingButtonAdd: FloatingActionButton
    lateinit var taskAdapter: TaskAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerViewSingleTask: RecyclerView

    var dbTaskList = arrayListOf<TaskEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layoutManager = LinearLayoutManager(this)
        recyclerViewSingleTask = findViewById(R.id.taskLayout)

        init()

        floatingButtonAdd=findViewById(R.id.floating_action_button)
        floatingButtonAdd.setOnClickListener {
            openDialog()
        }

    }

    private fun init() {
        dbTaskList = RetrieveTasks(this).execute().get() as ArrayList<TaskEntity>
        taskAdapter = TaskAdapter(this, dbTaskList,object : TaskAdapter.OnItemClickListener {
            override fun onDeleteClick(taskId:Int) {
                init()
            }

            override fun onUpdate(taskId: Int) {
                init()
            }
        })
        recyclerViewSingleTask.adapter = taskAdapter
        recyclerViewSingleTask.layoutManager = layoutManager
        itemTouchHelper.attachToRecyclerView(recyclerViewSingleTask)
    }

    private fun openDialog()
    {
        val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.add_new_task_dialog, null)
        val dialogBuilder:MaterialAlertDialogBuilder= MaterialAlertDialogBuilder(this,R.style.RoundShapeTheme)
            .setView(view)
            .setPositiveButton(getString(R.string.add)) { dialog, _: Int ->

                val taskEntity=TaskEntity(view.textFieldNewTask.text.toString(),0)
                val result= DBAsyncTask(this, taskEntity, 2).execute().get()
                if(result)
                {
                    Toast.makeText(this,"Task Added",Toast.LENGTH_LONG).show()
                    taskAdapter.notifyDataSetChanged()
                    dialog.dismiss()
                    init()

                }
                else
                {
                    Toast.makeText(this,"Some Error Occurred!",Toast.LENGTH_LONG).show()
                }
                taskAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        dialogBuilder.create()
        dialogBuilder.show()
    }


    class RetrieveTasks(private val context: Context) : AsyncTask<Void, Void, List<TaskEntity>>() {
        override fun doInBackground(vararg params: Void?): List<TaskEntity> {
            val db = Room.databaseBuilder(context, TaskDatabase::class.java, "asks012-db").build()

            return db.taskDao().getAllTasks()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater:MenuInflater=menuInflater
        inflater.inflate(R.menu.toolbar_menu,menu)

        val searchItem : MenuItem? = menu?.findItem(R.id.action_search)
       val searchView: SearchView= searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                taskAdapter.filter.filter(newText)
                return false
            }

        })
        return true
    }

    private var itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: ViewHolder, target: ViewHolder
            ): Boolean {
              return false
            }

            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                val taskEntity=viewHolder.itemView.tag
                val result = DBAsyncTask(applicationContext, taskEntity as TaskEntity, 3).execute().get()
                if (result) {

                    Toast.makeText(applicationContext, "Task Deleted ", Toast.LENGTH_SHORT).show()
                    init()

                } else {
                    Toast.makeText(applicationContext, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        })




}
