package com.davenet.notely.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.davenet.notely.database.NoteDao
import com.davenet.notely.database.asDomainModel
import com.davenet.notely.database.asDomainModelEntry
import com.davenet.notely.domain.NoteEntry
import com.davenet.notely.domain.asDataBaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

    val notes: LiveData<List<NoteEntry>> by lazy {
        Transformations.map(noteDao.getAllNotes()) {
            it.asDomainModel()
        }
    }

    val emptyNote: NoteEntry
        get() {
            return NoteEntry()
        }

    /**
     * Delete the Notes with the included ids from the database
     *
     * @param idList [List]<[Int]>
     */
    suspend fun deleteNotes(idList: List<Int>) {
        withContext(Dispatchers.IO) {
            noteDao.deleteSomeNotes(idList)
        }
    }

    /**
     * Insert a list of Notes into the database
     *
     * @param noteList [List]<[NoteEntry]>
     */
    suspend fun insertNotes(noteList: List<NoteEntry>) {
        withContext(Dispatchers.IO) {
            noteDao.insertNotesList(noteList.asDataBaseModel())
        }
    }

    /**
     * Retrieve a single note with the specified id from the database
     *
     * @param noteId [Int]
     * @return [Flow]<[NoteEntry]?>
     */
    suspend fun getNote(noteId: Int): Flow<NoteEntry?> {
        return flow {
            emit(noteDao.get(noteId).asDomainModelEntry())
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Retrieve the latest inserted note from the database
     *
     * @return [Flow]<[NoteEntry?]>
     */
    suspend fun getLatestNote(): Flow<NoteEntry?> {
        return flow {
            emit(noteDao.getNote().asDomainModelEntry())
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Insert a single note into the database
     *
     * @param note [NoteEntry]
     */
    suspend fun insertNote(note: NoteEntry) {
        withContext(Dispatchers.IO) {
            noteDao.insert(note.copy())
        }
    }

    /**
     * Update contents of a Note in the database
     *
     * @param note [NoteEntry]
     */
    suspend fun updateNote(note: NoteEntry) {
        withContext(Dispatchers.IO) {
            noteDao.update(note.copy())
        }
    }

    /**
     * Delete the Note with the specified id from the database
     *
     * @param id [Int]
     */
    suspend fun deleteNote(id: Int) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(id)
        }
    }
}