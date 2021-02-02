package com.davenet.notely.repository

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.davenet.notely.database.DatabaseNote.Companion.toDatabaseEntry
import com.davenet.notely.database.NoteDao
import com.davenet.notely.database.asDomainModel
import com.davenet.notely.database.toDatabaseList
import com.davenet.notely.domain.NoteEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    @VisibleForTesting(otherwise = PRIVATE)
    internal val noteDao: NoteDao
) {

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
     * @param idList list of ids of notes to be deleted
     */
    suspend fun deleteNotes(idList: List<Int>) {
        noteDao.deleteSomeNotes(idList)
    }

    /**
     * Insert a list of Notes into the database
     *
     * @param noteList list of notes to be inserted
     */
    suspend fun insertNotes(noteList: List<NoteEntry>) {
        noteDao.insertNotesList(noteList.toDatabaseList())
    }

    /**
     * Retrieve a single note with the specified id from the database
     *
     * @param noteId id of the note to be retrieved
     * @return a single note via a flow collector
     */
    fun getNote(noteId: Int): Flow<NoteEntry?> {
        return flow {
            emit(noteDao.get(noteId).asDomainModelEntry())
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Retrieve the latest inserted note from the database
     *
     * @return the latest inserted note via a flow collector
     */
    fun getLatestNote(): Flow<NoteEntry?> {
        return flow {
            emit(noteDao.getNote().asDomainModelEntry())
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Insert a single note into the database
     *
     * @param note the note to be inserted
     */
    suspend fun insertNote(note: NoteEntry) {
        noteDao.insert(toDatabaseEntry(note))
    }

    /**
     * Update contents of a Note in the database
     *
     * @param note the note to be updated
     */
    suspend fun updateNote(note: NoteEntry) {
        noteDao.update(toDatabaseEntry(note))
    }

    /**
     * Delete the Note with the specified id from the database
     *
     * @param id id of the note to be deleted
     */
    suspend fun deleteNote(id: Int) {
        noteDao.deleteNote(id)
    }
}