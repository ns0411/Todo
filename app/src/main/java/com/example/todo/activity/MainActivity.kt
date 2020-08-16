package com.example.todo.activity

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.room.Room
import com.example.todo.R
import com.example.todo.adapter.TaskAdapter
import com.example.todo.broadcastReciever.NotificationBroadcastReceiver
import com.example.todo.database.DBAsyncTask
import com.example.todo.database.TaskDatabase
import com.example.todo.database.TaskEntity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.android.synthetic.main.add_new_task_dialog.view.*
import java.util.*


class MainActivity : AppCompatActivity(), View.OnLongClickListener {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var floatingButtonAdd: FloatingActionButton
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerViewSingleTask: RecyclerView
    private lateinit var deleteIcon: Drawable
    private lateinit var noTaskAdded: ShapeableImageView
    private var swipeBackgroundColor: ColorDrawable = ColorDrawable(Color.parseColor("#FF0000"))
    private var date = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0
    private var counter = 0
    private lateinit var actionMode: ActionMode
    private var notText: String = ""
    var isContextModeEnabled = false

    private var dbTaskList = arrayListOf<TaskEntity>()
    private var selectedItemsList = arrayListOf<TaskEntity>()

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()

        noTaskAdded = findViewById(R.id.noTasks)

        toolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_bin_sweep)!!
        layoutManager = LinearLayoutManager(this)
        recyclerViewSingleTask = findViewById(R.id.taskLayout)

        init()

        floatingButtonAdd = findViewById(R.id.floating_action_button)

        floatingButtonAdd.setOnClickListener {
            openDialog()
        }
    }

    private fun init() {
        dbTaskList = RetrieveTasks(this).execute().get() as ArrayList<TaskEntity>

        if (dbTaskList.isEmpty()) {
            noTaskAdded.visibility = View.VISIBLE
        } else {
            noTaskAdded.visibility = View.INVISIBLE
        }


        taskAdapter = TaskAdapter(this, dbTaskList, object : TaskAdapter.OnItemClickListener {
            override fun onDeleteClick(taskId: Int) {
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
    private fun openDialog() {
        var dateReminder = ""
        var timeReminder = ""
        val view =
            LayoutInflater.from(this@MainActivity).inflate(R.layout.add_new_task_dialog, null)
        val dialogBuilder: MaterialAlertDialogBuilder =
            MaterialAlertDialogBuilder(this, R.style.RoundShapeTheme)
                .setView(view)

        view.setReminderChip.setOnClickListener {
            val viewPicker =
                LayoutInflater.from(this).inflate(R.layout.select_date_time_dialog, null)
            val dialogBuilder1: MaterialAlertDialogBuilder =
                MaterialAlertDialogBuilder(this, R.style.RoundShapeTheme)
                    .setView(viewPicker)
            val timePicker: TimePicker = viewPicker.findViewById(R.id.timePicker)
            timePicker.setIs24HourView(true)
            hour = timePicker.hour
            minute = timePicker.minute

            timePicker.setOnTimeChangedListener { _, _, _ ->
                hour = timePicker.hour
                minute = timePicker.minute

            }

            val datePicker: DatePicker = viewPicker.findViewById(R.id.datePicker)
            datePicker.minDate = System.currentTimeMillis() - 1000
            date = datePicker.dayOfMonth
            month = datePicker.month
            year = datePicker.year

            datePicker.setOnDateChangedListener { _, _, _, _ ->
                date = datePicker.dayOfMonth
                month = datePicker.month
                year = datePicker.year

            }

            dialogBuilder1.setPositiveButton("Set Reminder") { dialog, _ ->

                dateReminder =
                    date.toString() + "/" + (month + 1).toString() + "/" + year.toString()
                timeReminder = if (hour < 10) {
                    if (minute < 10)
                        "0$hour:0$minute"
                    else
                        "0$hour:$minute"
                } else
                    "$hour:$minute"

                view.setReminderChip.text = "$dateReminder  $timeReminder"
                view.setReminderChip.tag = "set"
                dialog.dismiss()

            }

            dialogBuilder1.create()
            dialogBuilder1.show()
        }

        dialogBuilder.setPositiveButton(getString(R.string.add)) { dialog, _: Int ->

            if (view.textFieldNewTask.text.toString().isEmpty()) {
                Toast.makeText(this, "Your task can't be Empty, Please try again", Toast.LENGTH_LONG).show()
            }else {
                val idReminder = (Date().time / 1000L % Int.MAX_VALUE).toInt()
                val taskEntity = TaskEntity(view.textFieldNewTask.text.toString(), dateReminder, timeReminder, false, 0, idReminder)
                val result = DBAsyncTask(this, taskEntity, 2).execute().get()
                if (result) {
                    if (view.setReminderChip.tag == "set") {
                        scheduleNotification(
                            year,
                            month,
                            date,
                            hour,
                            minute,
                            view.textFieldNewTask.text.toString(),
                            idReminder
                        )
                    }
                    taskAdapter.notifyDataSetChanged()
                    dialog.dismiss()
                    init()
                    notText = view.textFieldNewTask.text.toString()

                } else {
                    Toast.makeText(this, "Error while adding your task", Toast.LENGTH_LONG).show()
                }
            }
        }
        taskAdapter.notifyDataSetChanged()

    dialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialog, _->

        dialog.dismiss()
    }
    dialogBuilder.create()
    dialogBuilder.show()
}

     fun scheduleNotification(
         yearS: Int,
         monthS: Int,
         dateS: Int,
         hourS: Int,
         minuteS: Int,
         text: String,
         id: Int
     )
     {
        val calendar:Calendar= Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.clear()
        calendar.set(yearS,monthS,dateS,hourS,minuteS,0)

        val intent =Intent(applicationContext,NotificationBroadcastReceiver::class.java)
            intent.putExtra("content",text)
            intent.putExtra("id",id)
         val pendingIntent=PendingIntent.getBroadcast(applicationContext,id,intent,PendingIntent.FLAG_UPDATE_CURRENT)
         val alarmManager= applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
         alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
     }


    fun cancelNotification(id:Int, text:String)
    {
        val intent =Intent(applicationContext,NotificationBroadcastReceiver::class.java)
        intent.putExtra("content",text)
        intent.putExtra("id",id)
        val pendingIntent=PendingIntent.getBroadcast(applicationContext,id,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager= applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

    }



    class RetrieveTasks(private val context: Context) : AsyncTask<Void, Void, List<TaskEntity>>() {
        override fun doInBackground(vararg params: Void?): List<TaskEntity> {
            val db = Room.databaseBuilder(context, TaskDatabase::class.java, "todo_tasks_12-db").build()

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


            @RequiresApi(Build.VERSION_CODES.O)
            override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                val taskEntity:TaskEntity=viewHolder.itemView.tag as TaskEntity
                val id=taskEntity.alarmId
                val content=taskEntity.taskContent
                val dateFromDb=taskEntity.date
                val timeFromDb=taskEntity.timeReminder

                val result = DBAsyncTask(applicationContext, taskEntity, 3).execute().get()

                if (result){
                    if(dateFromDb!="" && timeFromDb!="")
                    {
                        cancelNotification(id,content)
                    }
                        init()

                }else{
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

     private val callback = object : ActionMode.Callback {

        override fun onActionItemClicked(
            mode: ActionMode?,
            item: MenuItem?
        ): Boolean {
            return when (item?.itemId) {
                R.id.delete -> {
                     var counterDelete=0
                    for(items:TaskEntity in selectedItemsList)
                  {
                      val id=items.alarmId
                      val content=items.taskContent
                      val dateFromDb=items.date
                      val timeFromDb=items.timeReminder
                   if(DBAsyncTask(applicationContext,items,3).execute().get())
                       counterDelete++
                      if(dateFromDb!="" && timeFromDb!="")
                      {
                          cancelNotification(id,content)
                      }
                  }
                    if(counterDelete==selectedItemsList.size)
                    {
                        counter=0
                        updateCounter(counter)
                        mode?.finish()
                        floatingButtonAdd.visibility=View.VISIBLE
                    }
                    init()
                    true
                }

                else -> false
            }
        }

        override fun onCreateActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            menuInflater.inflate(R.menu.toolbar_menu_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            isContextModeEnabled=false
            taskAdapter.notifyDataSetChanged()
            counter=0
            selectedItemsList.clear()
            floatingButtonAdd.visibility=View.VISIBLE
        }
    }



    override fun onLongClick(v: View?): Boolean {
        floatingButtonAdd.visibility=View.INVISIBLE
        actionMode = this.startSupportActionMode(callback)!!
        actionMode.title = "0 Item selected"
        isContextModeEnabled=true
        taskAdapter.notifyDataSetChanged()
        return true
    }

    fun prepareItems(v: View, position: Int) {

        if((v as MaterialCheckBox).isChecked)
        {
            selectedItemsList.add(dbTaskList[position])
            counter++
            updateCounter(counter)

        }
        else{
            selectedItemsList.remove(dbTaskList[position])
            counter--
            updateCounter(counter)
        }
    }

    private fun updateCounter(count:Int)
    {
        if(count==0)
        {
            actionMode.title="O Item Selected"
        } else
        {
            actionMode.title="$count Items Selected"
        }
    }

    private fun createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel=NotificationChannel("notifyTask","hell", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description=notText

            val notificationManager=getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(notificationChannel)
        }
    }


}
