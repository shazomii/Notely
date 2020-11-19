package com.davenet.notely.repository

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.davenet.notely.broadcastreceiver.AlarmBroadcastReceiver
import com.davenet.notely.database.NotesDatabase
import com.davenet.notely.database.asDomainModelEntry
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.util.Constants
import com.davenet.notely.util.currentDate
import com.davenet.notely.util.formatReminderDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class NoteRepository(private val database: NotesDatabase) {

    fun getSelectedNote(noteId: Int): MutableLiveData<NoteEntry?> {
        return Transformations.map(database.noteDao.get(noteId)) {
            it.asDomainModelEntry()
        } as MutableLiveData<NoteEntry?>
    }

    val emptyNote: NoteEntry
        get() {
            return NoteEntry(id = null, title = "", text = "", date = null, reminder = null, started = false)
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
                note.reminder = pickedDateTime.timeInMillis
                reminder.text = formatReminderDate(pickedDateTime.timeInMillis)
            }, hour, minute, false).show()
        }, startYear, startMonth, startDay).show()
    }

    fun schedule(context: Context, note: NoteEntry) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmBroadcastReceiver::class.java).also {
            it.putExtra(Constants.NOTE_ID, note.id)
            it.putExtra(Constants.NOTE_TITLE, note.title)
        }

        val reminderPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context, note.id!!, intent, 0)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            note.reminder!!,
            reminderPendingIntent
        )

        note.started = true
    }

    fun cancelAlarm(context: Context, note: NoteEntry) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmBroadcastReceiver::class.java)

        val reminderPendingIntent = PendingIntent.getBroadcast(
            context,
            note.id!!,
            intent,
            0
        )

        alarmManager.cancel(reminderPendingIntent)

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