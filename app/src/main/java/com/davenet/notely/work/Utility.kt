package com.davenet.notely.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.util.NOTE_ID
import com.davenet.notely.util.NOTE_TITLE
import com.davenet.notely.util.currentDate
import java.util.concurrent.TimeUnit

fun createSchedule(context: Context, note: NoteEntry) {
    val data = Data.Builder()
        .putInt(NOTE_ID, note.id!!)
        .putString(NOTE_TITLE, note.title)
        .build()

    val delay = note.reminder!! - currentDate().timeInMillis

    note.started = true

    scheduleReminder(delay, data, context)
}

private fun scheduleReminder(delay: Long, data: Data, context: Context) {
    val reminderWork = OneTimeWorkRequest.Builder(NotifyWork::class.java)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .addTag("${context.packageName}.work.NotifyWork")
        .build()

    val workName = "Work ${data.getInt(NOTE_ID, 0)}"

    val instanceWorkManager = WorkManager.getInstance(context)
    instanceWorkManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, reminderWork)
}

fun cancelAlarm(context: Context, note: NoteEntry) {
    val workName = "Work ${note.id}"
    val instanceWorkManager = WorkManager.getInstance(context)
    instanceWorkManager.cancelUniqueWork(workName)
}