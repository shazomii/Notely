package com.davenet.notely.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavDeepLinkBuilder
import com.davenet.notely.R
import com.davenet.notely.util.Constants

class AlarmService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val noteId = intent?.getIntExtra(Constants.NOTE_ID, 0)
        val bundle = Bundle()
        bundle.putInt(Constants.NOTE_ID, noteId!!)

        val deepLink = NavDeepLinkBuilder(baseContext)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.loginFragment)
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
        val drawable = applicationInfo.loadIcon(packageManager)
        val bitmap = drawable.toBitmap()

        val builder = NotificationCompat.Builder(baseContext, Constants.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(deepLink)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setLargeIcon(bitmap)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setBigContentTitle(intent.getStringExtra(Constants.NOTE_TITLE))
                    .setSummaryText("Reminder")
            )
            .setContentTitle("Reminder")
            .setContentText(intent.getStringExtra(Constants.NOTE_TITLE))
            .build()

        notificationManager.notify(noteId, builder)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}