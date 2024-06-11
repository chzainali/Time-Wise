package com.example.timewise.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timewise.database.DatabaseHelper
import com.example.timewise.model.ChunkModel
import com.example.timewise.model.HelperClass

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val databaseHelper = DatabaseHelper(context)
        val chunkId = intent.getLongExtra("chunkId", -1)
        if (chunkId != -1L) {
            val chunkModel: ChunkModel =
                databaseHelper.getChunk(HelperClass.users?.id!!.toInt(), chunkId.toInt())!!
            val message = "Your have " + chunkModel.title + "."
            val message2 =
                "From (" + chunkModel.startTime + ") to (" + chunkModel.endTime + ")"
            // Handle the notification here
            showNotification(context, "Workout Reminder", message, message2)
        }
    }

    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        message2: String
    ) {
        val notificationHelper = NotificationHelper(context)
        val notification = notificationHelper.createNotification(title, message, message2)
        notificationHelper.notificationManager.notify(1, notification)
    }
}