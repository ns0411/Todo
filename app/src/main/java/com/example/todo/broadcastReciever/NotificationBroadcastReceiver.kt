package com.example.todo.broadcastReciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.todo.R

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
       // val text= intent?.getStringExtra("context")
       val builder: NotificationCompat.Builder? = context?.let { NotificationCompat.Builder(it,"notifyTask") }
           ?.setSmallIcon(R.drawable.ic_close)
           ?.setContentTitle("To-Do")
           ?.setContentText("jojojefj")
           ?.setPriority(NotificationCompat.PRIORITY_HIGH)

        if (builder != null) {
            context.let { NotificationManagerCompat.from(it) }.notify(200,builder.build())
        }


    }
}