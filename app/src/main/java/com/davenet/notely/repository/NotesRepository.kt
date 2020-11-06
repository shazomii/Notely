package com.davenet.notely.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.davenet.notely.database.NotesDatabase
import com.davenet.notely.database.asDomainModel
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.domain.asDataBaseModel
import com.davenet.notely.util.currentDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesRepository(private val database: NotesDatabase) {
    val notes: LiveData<List<NoteEntry>> = Transformations.map(database.noteDao.getAllNotes()) {
        it.asDomainModel()
    }

    val emptyNote: NoteEntry
        get() {
            return NoteEntry(id = null, "", text = "", date = null)
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

    suspend fun insertNote(note: NoteEntry) {
        withContext(Dispatchers.IO) {
            database.noteDao.insert(note.copy(date = currentDate()).copy())
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

    suspend fun updateNote(note: NoteEntry) {
        withContext(Dispatchers.IO) {
            database.noteDao.update(note.copy(date = currentDate()).copy())
        }
    }
}