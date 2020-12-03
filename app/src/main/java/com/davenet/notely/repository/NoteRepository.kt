package com.davenet.notely.repository

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.TextView
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.davenet.notely.R
import com.davenet.notely.database.NotesDatabase
import com.davenet.notely.database.asDomainModelEntry
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.util.Constants
import com.davenet.notely.util.colors
import com.davenet.notely.util.currentDate
import com.davenet.notely.util.formatReminderDate
import com.davenet.notely.work.NotifyWork
import kotlinx.android.synthetic.main.fragment_edit_note.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import petrov.kristiyan.colorpicker.ColorPicker
import petrov.kristiyan.colorpicker.ColorPicker.OnChooseColorListener
import java.util.*
import java.util.concurrent.TimeUnit

class NoteRepository(private val database: NotesDatabase) {
    val color = colors.random()

    suspend fun getNote(noteId: Int): Flow<NoteEntry?> {
        return flow {
            emit(database.noteDao.get(noteId).asDomainModelEntry())
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getLatestNote(): Flow<NoteEntry?> {
        return flow {
            emit(database.noteDao.getNote().asDomainModelEntry())
        }.flowOn(Dispatchers.IO)
    }

    val emptyNote: NoteEntry
        get() {
            return NoteEntry(null, "", "", null, null, false, color)
        }

    fun pickDateTime(context: Context, note: NoteEntry, reminder: TextView) {
        val currentDateTime = currentDate()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val hour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentDateTime.get(Calendar.MINUTE)

        return DatePickerDialog(context, { _, year, month, day ->
            TimePickerDialog(context, { _, hour, minute ->
                val pickedDateTime = currentDate()
                pickedDateTime.set(year, month, day, hour, minute)
                if (pickedDateTime.timeInMillis <= currentDate().timeInMillis) {
                    pickedDateTime.run {
                        set(Calendar.DAY_OF_MONTH, currentDateTime.get(Calendar.DAY_OF_MONTH) + 1)
                        set(Calendar.YEAR, currentDateTime.get(Calendar.YEAR))
                        set(Calendar.MONTH, currentDateTime.get(Calendar.MONTH))
                    }
                }
                note.reminder = pickedDateTime.timeInMillis
                reminder.text = formatReminderDate(pickedDateTime.timeInMillis)
            }, hour, minute, false).show()
        }, startYear, startMonth, startDay).show()
    }

    fun pickColor(activity: Activity, note: NoteEntry) {
        val colorPicker = ColorPicker(activity)
        colorPicker.run {
            setRoundColorButton(true)
                .setTitle(activity.getString(R.string.note_color))
                .show()
            setOnChooseColorListener(object : OnChooseColorListener {
                override fun onChooseColor(position: Int, color: Int) {
                    note.color = color
                    activity.editNoteLayout.setBackgroundColor(color)
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

        note.reminder = null
        note.started = false
    }

    suspend fun insertNote(note: NoteEntry) {
        withContext(Dispatchers.IO) {
            database.noteDao.insert(note.copy())
        }
    }

    suspend fun updateNote(note: NoteEntry) {
        withContext(Dispatchers.IO) {
            database.noteDao.update(note.copy())
        }
    }
}