package com.example.todo.broadcastReciever

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.todo.R
import com.example.todo.activity.MainActivity

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val text= intent?.getStringExtra("content")
        val id= intent?.getIntExtra("id",-1)

        val resultIntent=Intent(context,MainActivity::class.java)
        val pendingIntent= id?.let {
            PendingIntent.getActivity(context,
                it,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder: NotificationCompat.Builder? = context?.let { NotificationCompat.Builder(it,"notifyTask") }
           ?.setSmallIcon(R.drawable.ic_reminder)
           ?.setContentTitle("Reminder")
           ?.setContentText(text)
           ?.setPriority(NotificationCompat.PRIORITY_HIGH)
            ?.setContentIntent(pendingIntent)
            ?.setAutoCancel(true)

        val notificationManagerCompat= context?.let { NotificationManagerCompat.from(it) }
                if (builder != null) {
                    if (id != null) {
                        notificationManagerCompat?.notify(id, builder.build())
                    }
                 }

    }
}