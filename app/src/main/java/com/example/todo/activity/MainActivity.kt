package com.example.todo.activity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.add_new_task_dialog.view.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), View.OnLongClickListener {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var floatingButtonAdd: FloatingActionButton
    lateinit var taskAdapter: TaskAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerViewSingleTask: RecyclerView
    private lateinit var deleteIcon: Drawable
    private var swipeBackgroundColor: ColorDrawable= ColorDrawable(Color.parseColor("#FF0000"))
    private var date=0
    private var month=0
    private var year=0
    private var hour=0
    private var minute=0

     var isContextModeEnabled=false

    private var dbTaskList = arrayListOf<TaskEntity>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar=findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        deleteIcon=ContextCompat.getDrawable(this,R.drawable.ic_bin)!!
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openDialog()
    {
        var dateReminder=""
        var timeReminder=""
        val view = LayoutInflater.from(this@MainActivity).inflate(R.layout.add_new_task_dialog, null)
        val dialogBuilder:MaterialAlertDialogBuilder= MaterialAlertDialogBuilder(this,R.style.RoundShapeTheme)
            .setView(view)

        view.setReminderChip.setOnClickListener {
            val viewPicker = LayoutInflater.from(this).inflate(R.layout.select_date_time_dialog, null)
            val dialogBuilder1: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this,R.style.RoundShapeTheme)
                .setView(viewPicker)
            val timePicker: TimePicker =viewPicker.findViewById(R.id.timePicker)
            timePicker.setIs24HourView(true)
            hour=timePicker.hour
            minute=timePicker.minute

            timePicker.setOnTimeChangedListener{_,_,_ ->
                hour=timePicker.hour
                minute=timePicker.minute
            }

            val datePicker: DatePicker =viewPicker.findViewById(R.id.datePicker)
            datePicker.minDate= Calendar.DAY_OF_MONTH.toLong()
            date=datePicker.dayOfMonth
            month=datePicker.month
            year=datePicker.year

            datePicker.setOnDateChangedListener { _,_,_,_ ->
                date=datePicker.dayOfMonth
                month=datePicker.month
                year=datePicker.year
            }


            dialogBuilder1.setPositiveButton("Set Reminder"){dialog,_ ->

                dateReminder=date.toString()+"/"+(month+1).toString()+"/"+year.toString()
                timeReminder= "$hour:$minute"
                view.setReminderChip.text=dateReminder+"  "+timeReminder
                dialog.dismiss()
            }

            dialogBuilder1.create()
            dialogBuilder1.show()
        }

            dialogBuilder.setPositiveButton(getString(R.string.add)) { dialog, _: Int ->

                if(view.textFieldNewTask.text.toString().isEmpty())
                {
                    Toast.makeText(this,"Your task can't be Empty, Please try again",Toast.LENGTH_LONG).show()
                }
                else{
                val taskEntity=TaskEntity(view.textFieldNewTask.text.toString(),dateReminder,timeReminder,false,0)
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
                    Toast.makeText(this,"Error while adding your task",Toast.LENGTH_LONG).show()

                }}
                taskAdapter.notifyDataSetChanged()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        dialogBuilder.create()
        dialogBuilder.show()
    }


    class RetrieveTasks(private val context: Context) : AsyncTask<Void, Void, List<TaskEntity>>() {
        override fun doInBackground(vararg params: Void?): List<TaskEntity> {
            val db = Room.databaseBuilder(context, TaskDatabase::class.java, "todo_tasks-db").build()

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

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView=viewHolder.itemView
            val iconMargin=(itemView.height-deleteIcon.intrinsicHeight)/2

            if(dX < 0)
            {
                swipeBackgroundColor.setBounds(itemView.right+dX.toInt(),itemView.top,itemView.right,itemView.bottom)
                deleteIcon.setBounds(itemView.right-iconMargin-deleteIcon.intrinsicWidth,itemView.top+iconMargin,itemView.right-iconMargin,itemView.bottom-iconMargin)
            }
            swipeBackgroundColor.draw(c)

            if(dX < 0)
            {
                c.clipRect(itemView.right+dX.toInt(),itemView.top,itemView.right,itemView.bottom)
            }

            deleteIcon.draw(c)
            c.restore()

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
        })

    override fun onLongClick(v: View?): Boolean {
        isContextModeEnabled=true

        return true
    }


}
