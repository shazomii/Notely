package com.davenet.notely.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.davenet.notely.R
import com.davenet.notely.util.Constants

class AlarmService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle = Bundle()
        bundle.putInt(Constants.NOTE_ID, intent?.getIntExtra(Constants.NOTE_ID, 0)!!)
        val deepLink = NavDeepLinkBuilder(baseContext)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.editNoteFragment)
            .setArguments(bundle)
            .createPendingIntent()

        val shareIntent: PendingIntent = PendingIntent.getActivity(
            baseContext, 0, Intent.createChooser(Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Constants.NOTE_TEXT)), null),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(baseContext, Constants.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(deepLink)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(intent.getStringExtra(Constants.NOTE_TEXT))
                .setBigContentTitle(intent.getStringExtra(Constants.NOTE_TITLE))
                .setSummaryText(intent.getStringExtra(Constants.NOTE_TITLE)))
            .addAction(R.drawable.ic_baseline_share_24, "Share", shareIntent)
            .build()

        startForeground(1, builder)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}