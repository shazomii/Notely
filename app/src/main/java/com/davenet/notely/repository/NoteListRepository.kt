package com.davenet.notely.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.davenet.notely.broadcastreceiver.AlarmBroadcastReceiver
import com.davenet.notely.database.NotesDatabase
import com.davenet.notely.database.asDomainModel
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.domain.asDataBaseModel
import com.davenet.notely.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteListRepository(private val database: NotesDatabase) {
    val notes: LiveData<List<NoteEntry>> = Transformations.map(database.noteDao.getAllNotes()) {
        it.asDomainModel()
    }




    suspend fun deleteAllNotes() {
        withContext(Dispatchers.IO) {
            database.noteDao.deleteAllNotes()
        }
    }

    suspend fun deleteNote(note: NoteEntry) {
        withContext(Dispatchers.IO) {
            database.noteDao.deleteNote(note.id)
        }
    }

    suspend fun restoreNote(note: NoteEntry) {
        withContext(Dispatchers.IO) {
            database.noteDao.insert(note.copy())
        }
    }

    suspend fun insertAllNotes(noteList: List<NoteEntry>) {
        withContext(Dispatchers.IO) {
            database.noteDao.insertNotesList(noteList.asDataBaseModel())
        }
    }

    fun schedule(context: Context, note: NoteEntry) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmBroadcastReceiver::class.java).also {
            it.putExtra("noteId", note.id)
            it.putExtra(Constants.NOTE_TITLE, note.title)
            it.putExtra(Constants.NOTE_TEXT, note.text)
        }

        val reminderPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            note.reminder!!,
            reminderPendingIntent
        )

        note.started = true
    }
}