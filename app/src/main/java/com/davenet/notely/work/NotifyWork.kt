package com.davenet.notely.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.davenet.notely.R
import com.davenet.notely.util.Constants

class NotifyWork(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val id = inputData.getInt(Constants.NOTE_ID, 0)
        val title = inputData.getString(Constants.NOTE_TITLE)

        sendNotification(id, title)

        return Result.success()
    }

    private fun sendNotification(id: Int, title: String?) {
        val bundle = Bundle()
        bundle.putInt(Constants.NOTE_ID, id)

        val deepLink = NavDeepLinkBuilder(applicationContext)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.noteListFragment)
            .setArguments(bundle)
            .createPendingIntent()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    Constants.CHANNEL_ID, "Notely Reminder", NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
        val drawable =
            applicationContext.applicationInfo.loadIcon(applicationContext.packageManager)
        val bitmap = drawable.toBitmap()

        val builder = NotificationCompat.Builder(applicationContext, Constants.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(deepLink)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setLargeIcon(bitmap)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setBigContentTitle("Reminder")
                    .addLine(title)
            )
            .setContentTitle("Reminder")
            .setContentText(title)
            .build()

        notificationManager.notify(id, builder)
    }

}