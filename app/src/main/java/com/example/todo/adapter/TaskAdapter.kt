package com.example.todo.adapter

import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.activity.MainActivity
import com.example.todo.database.DBAsyncTask
import com.example.todo.database.TaskEntity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.add_new_task_dialog.view.*
import kotlinx.android.synthetic.main.recyclerview_single_task.view.*
import java.util.*
import kotlin.collections.ArrayList

class TaskAdapter(private val context: Context, val taskContentList:ArrayList<TaskEntity>, private val listener: OnItemClickListener) :RecyclerView.Adapter<TaskAdapter.ViewHolderTask>(),Filterable
{

    private var date=0
    private var month=0
    private var year=0
    private var hour=0
    private var minute=0
    private var mainActivity:MainActivity=MainActivity()
    private lateinit var reminderDate:String
    private lateinit var reminderTime:String


    private var filteredTaskList =ArrayList<TaskEntity>()
    init {
        filteredTaskList= taskContentList
    }


    class ViewHolderTask(view: View): RecyclerView.ViewHolder(view) {

        val textTask: MaterialTextView =view.findViewById(R.id.textViewTask)
        val dateAndTime: MaterialTextView=view.findViewById(R.id.dateAndTime)
        val selectDeleteCheckBox: MaterialCheckBox=view.findViewById(R.id.checkboxSelectDelete)
        val llContent: MaterialCardView =view.findViewById(R.id.llContent)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTask {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_single_task,parent,false)
        return ViewHolderTask(view)
    }

    override fun getItemCount(): Int {
        return filteredTaskList.size
    }

    interface OnItemClickListener {
        fun onDeleteClick(taskId:Int)
        fun onUpdate(taskId:Int)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolderTask, position: Int) {
        val taskItemObject=filteredTaskList[filteredTaskList.size-position-1]
        val id=taskItemObject.taskId

        if(!taskItemObject.isCompleted)
        {

            holder.textTask.text=taskItemObject.taskContent
            holder.llContent.checkboxComplete.isChecked=false
        }
        if(taskItemObject.isCompleted)
        {
            val spannableString = SpannableString(taskItemObject.taskContent)
            spannableString.setSpan(StrikethroughSpan(), 0, spannableString.length, 0)
            holder.textTask.text = spannableString
            holder.llContent.checkboxComplete.isChecked=true
            holder.dateAndTime.text="Task completed"
            holder.llContent.isClickable=false
        }

        if(taskItemObject.date.isEmpty() && taskItemObject.timeReminder.isEmpty())
        {
                if (!taskItemObject.isCompleted)
                    holder.dateAndTime.text = "No reminder set"
                else
                    holder.dateAndTime.text = "Task Completed"
        }
        else
            holder.dateAndTime.text=taskItemObject.date+" "+taskItemObject.timeReminder

        holder.llContent.setOnLongClickListener(mainActivity)
        if(!mainActivity.isContextModeEnabled)
        {
            holder.selectDeleteCheckBox.visibility=View.GONE
        }else{
            holder.selectDeleteCheckBox.visibility=View.VISIBLE
            holder.selectDeleteCheckBox.isChecked=false
        }


        holder.llContent.setOnClickListener {

            val view = LayoutInflater.from(context).inflate(R.layout.add_new_task_dialog, null)

                if(taskItemObject.date.isEmpty() && taskItemObject.timeReminder.isEmpty())
                {
                    view.setReminderChip.text="Set Reminder"
                }
                else
                    view.setReminderChip.text=taskItemObject.date+" "+taskItemObject.timeReminder

            view.setReminderChip.setOnClickListener {
                val viewPicker = LayoutInflater.from(context).inflate(R.layout.select_date_time_dialog, null)
                val dialogBuilder1: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context,R.style.RoundShapeTheme)
                    .setView(viewPicker)
                val timePicker:TimePicker=viewPicker.findViewById(R.id.timePicker)
                timePicker.setIs24HourView(true)
                hour=timePicker.hour
                minute=timePicker.minute

                timePicker.setOnTimeChangedListener{_,_,_ ->
                    hour=timePicker.hour
                    minute=timePicker.minute
                }


                val datePicker:DatePicker=viewPicker.findViewById(R.id.datePicker)
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

                   reminderDate=date.toString()+"/"+(month+1).toString()+"/"+year.toString()
                   reminderTime= "$hour:$minute"


                    view.setReminderChip.text=reminderDate+"  "+reminderTime
                    dialog.dismiss()
                }
                dialogBuilder1.create()
                dialogBuilder1.show()
            }


            view.textFieldNewTask.setText(taskItemObject.taskContent)
            val dialogBuilder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context,R.style.RoundShapeTheme)
                .setView(view)
                .setPositiveButton("Update") { dialog, _: Int ->
                    if(view.textFieldNewTask.text.toString().isEmpty())
                    {
                        Toast.makeText(context,"Your task can't be Empty, Please try again",Toast.LENGTH_LONG).show()
                    }
                    else{
                    val taskEntity=TaskEntity(view.textFieldNewTask.text.toString(),reminderDate,reminderTime,taskItemObject.isCompleted,id)
                    val result = DBAsyncTask(context, taskEntity, 4).execute().get()
                    if(result) {

                        Toast.makeText(context, "Task Updated ", Toast.LENGTH_SHORT).show()
                        listener.onUpdate(taskItemObject.taskId)
                        dialog.dismiss()

                    } else {
                        Toast.makeText(context, "Error while updating ", Toast.LENGTH_SHORT).show()
                    }}
                }

            dialogBuilder.create()
            dialogBuilder.show()

        }

        holder.itemView.tag=TaskEntity(taskItemObject.taskContent,taskItemObject.date,taskItemObject.timeReminder,taskItemObject.isCompleted,id)


        holder.llContent.checkboxComplete.setOnClickListener {
            if (holder.llContent.checkboxComplete.isChecked) {

                val taskEntity = TaskEntity(taskItemObject.taskContent, taskItemObject.date,taskItemObject.timeReminder, true, id)
                if (DBAsyncTask(context, taskEntity, 4).execute().get()) {
                    val spannableString = SpannableString(taskItemObject.taskContent)
                    spannableString.setSpan(StrikethroughSpan(), 0, spannableString.length, 0)
                    holder.textTask.text = spannableString
                    holder.dateAndTime.text="Task completed"
                    holder.llContent.isClickable=false
                }
            } else {

                val taskEntity = TaskEntity(taskItemObject.taskContent, taskItemObject.date,taskItemObject.timeReminder, false, id)
                 if (DBAsyncTask(context, taskEntity, 4).execute().get())
                    holder.textTask.text = taskItemObject.taskContent
                    holder.dateAndTime.text="Set Reminder Again"
                    holder.llContent.isClickable=true

            }
        }



    }


    override fun getFilter(): Filter {
        return object :Filter()
        {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
               val textSearch=constraint.toString().toLowerCase(Locale.ROOT)
                if (textSearch.isEmpty())
                {
                    filteredTaskList= taskContentList
                }
                else
                {
                    val resultList= ArrayList<TaskEntity>()
                    for(row in taskContentList)
                    {
                        if(row.toString().toLowerCase(Locale.ROOT).contains(textSearch))
                        {
                            resultList.add(row)
                        }
                    }
                    filteredTaskList=resultList
                }
                val filteredResults= FilterResults()
                filteredResults.values=filteredTaskList
                return filteredResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
              filteredTaskList= results?.values as ArrayList<TaskEntity>
                notifyDataSetChanged()
            }

        }
    }
}
