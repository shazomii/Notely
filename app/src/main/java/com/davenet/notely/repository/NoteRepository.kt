package com.davenet.notely.repository

import com.davenet.notely.database.NotesDatabase
import com.davenet.notely.database.asDomainModelEntry
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.util.colors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

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