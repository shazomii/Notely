package com.davenet.notely.work

import android.app.Activity
import android.content.Context
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.davenet.notely.R
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.util.Constants
import com.davenet.notely.util.currentDate
import petrov.kristiyan.colorpicker.ColorPicker
import java.util.concurrent.TimeUnit

class Utility {
    fun pickColor(activity: Activity, note: NoteEntry) {
        val colorPicker = ColorPicker(activity)
        colorPicker.run {
            setRoundColorButton(true)
                .setTitle(activity.getString(R.string.note_color))
                .show()
            setOnChooseColorListener(object : ColorPicker.OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    note.color = color
                    activity.findViewById<ConstraintLayout>(R.id.editNoteLayout).setBackgroundColor(color)
                    activity.findViewById<CardView>(R.id.reminderCard).setCardBackgroundColor(color)
                }

                override fun onCancel() {
                    return
                }
            })
        }
    }

    fun createSchedule(context: Context, note: NoteEntry) {
        val data = Data.Builder()
            .putInt(Constants.NOTE_ID, note.id!!)
            .putString(Constants.NOTE_TITLE, note.title)
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

        val workName = "Work ${data.getInt(Constants.NOTE_ID, 0)}"

        val instanceWorkManager = WorkManager.getInstance(context)
        instanceWorkManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, reminderWork)
    }

    fun cancelAlarm(context: Context, note: NoteEntry) {
        val workName = "Work ${note.id}"
        val instanceWorkManager = WorkManager.getInstance(context)
        instanceWorkManager.cancelUniqueWork(workName)
    }
}