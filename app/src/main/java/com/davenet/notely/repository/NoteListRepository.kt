package com.davenet.notely.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.davenet.notely.database.NotesDatabase
import com.davenet.notely.database.asDomainModel
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.domain.asDataBaseModel
import com.davenet.notely.work.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteListRepository(private val database: NotesDatabase) {
    private val utility = Utility()
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

    fun createSchedule(context: Context, note: NoteEntry) {
        utility.createSchedule(context, note)
    }

    fun cancelAlarm(context: Context, note: NoteEntry) {
        utility.cancelAlarm(context, note)
    }
}