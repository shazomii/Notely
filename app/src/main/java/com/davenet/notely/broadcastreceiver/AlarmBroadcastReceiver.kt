package com.davenet.notely.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.davenet.notely.service.AlarmService
import com.davenet.notely.service.RescheduleAlarmsService
import com.davenet.notely.util.Constants

class AlarmBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
            val toastText = String.format("Rescheduling Notely Reminders")
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
            startRescheduleAlarmsService(context)
        } else {
            startAlarmService(context, intent)
        }
    }

    private fun startAlarmService(context: Context?, intent: Intent?) {
        val intentService = Intent(context, AlarmService::class.java).also { i ->
            i.putExtra(Constants.NOTE_ID, intent?.getIntExtra(Constants.NOTE_ID, 10))
            i.putExtra(Constants.NOTE_TITLE, intent?.getStringExtra(Constants.NOTE_TITLE))
            i.putExtra(Constants.NOTE_TEXT, intent?.getStringExtra(Constants.NOTE_TEXT))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(intentService)
        } else {
            context?.startService(intentService)
        }
    }

    private fun startRescheduleAlarmsService(context: Context?) {
        val intentService = Intent(context, RescheduleAlarmsService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(intentService)
        } else {
            context?.startService(intentService)
        }
    }
}