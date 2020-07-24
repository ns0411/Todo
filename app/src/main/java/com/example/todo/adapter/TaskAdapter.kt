package com.example.todo.adapter

import android.content.Context
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.todo.R
import com.example.todo.database.DBAsyncTask
import com.example.todo.database.TaskDatabase
import com.example.todo.database.TaskEntity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.add_new_task_dialog.view.*
import kotlinx.android.synthetic.main.recyclerview_single_task.view.*
import java.util.*
import kotlin.collections.ArrayList

class TaskAdapter(val context: Context, val taskContentList:ArrayList<TaskEntity>,private val listener: OnItemClickListener) :RecyclerView.Adapter<TaskAdapter.ViewHolderTask>(),Filterable
{

    private var filteredTaskList =ArrayList<TaskEntity>()
    init {
        filteredTaskList= taskContentList
    }


    class ViewHolderTask(view: View): RecyclerView.ViewHolder(view) {
        val textTask: MaterialTextView =view.findViewById(R.id.textViewTask)
        val llContent: MaterialCardView =view.findViewById(R.id.llContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.ViewHolderTask {
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

    override fun onBindViewHolder(holder: TaskAdapter.ViewHolderTask, position: Int) {
        val taskItemObject=filteredTaskList[filteredTaskList.size-position-1]
        holder.textTask.text=taskItemObject.taskContent

        holder.llContent.deleteTaskBtn.setOnClickListener(View.OnClickListener {
            val taskEntity=TaskEntity(taskItemObject.taskContent,taskItemObject.taskId)
            val result = DBAsyncTask(context,taskEntity , 3).execute().get()
            if (result) {

                Toast.makeText(context, "Task Deleted ", Toast.LENGTH_SHORT).show()
                notifyItemRemoved(position)
                listener.onDeleteClick(taskItemObject.taskId)

            } else {
                Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
            }
        })

        holder.llContent.setOnClickListener(View.OnClickListener {

            val view = LayoutInflater.from(context).inflate(R.layout.add_new_task_dialog, null)
            view.textFieldNewTask.setText(holder.textTask.text)
            val dialogBuilder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context,R.style.RoundShapeTheme)
                .setView(view)
                .setPositiveButton("Update") { dialog, _: Int ->
                    val taskEntity=TaskEntity(view.textFieldNewTask.text.toString(),taskItemObject.taskId)
                    val result = DBAsyncTask(context,taskEntity,4).execute().get()
                    if(result)
                    {

                    Toast.makeText(context, "Task Updated ", Toast.LENGTH_SHORT).show()
                    listener.onUpdate(taskItemObject.taskId)
                }
            else
            {
                Toast.makeText(context, "Some Error Occurred ", Toast.LENGTH_SHORT).show()
            }
                }

            dialogBuilder.create()
            dialogBuilder.show()

        })
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
