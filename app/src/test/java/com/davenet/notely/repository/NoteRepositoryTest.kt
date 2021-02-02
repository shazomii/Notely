package com.davenet.notely.repository

import com.davenet.notely.database.DatabaseNote
import com.davenet.notely.database.DatabaseNote.Companion.toDatabaseEntry
import com.davenet.notely.database.NoteDao
import com.davenet.notely.database.toDatabaseList
import com.davenet.notely.domain.NoteEntry
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NoteRepositoryTest {

    @MockK(relaxed = true)
    lateinit var noteDao: NoteDao

    lateinit var noteRepository: NoteRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        noteRepository = NoteRepository(noteDao)
    }

    @Test
    fun getEmptyNote_ShouldReturnEmptyNote() {
        val emptyNote = noteRepository.emptyNote

        assertEquals(NoteEntry().copy(color = emptyNote.color), emptyNote)
    }

    @Test
    fun getAllNotes_ShouldInvokeCorrectDaoMethod() {
        noteRepository.notes

        verify { noteDao.getAllNotes() }
    }

    @Test
    fun deleteNotes_ShouldInvokeCorrectDaoMethod() = runBlocking {
        val idList = listOf(2, 3)
        val idListCaptor = slot<List<Int>>()

        noteRepository.deleteNotes(idList)

        coVerify {
            noteDao.deleteSomeNotes(capture(idListCaptor))
        }
        assertEquals(idList, idListCaptor.captured)
    }

    @Test
    fun insertNotes_ShouldInvokeCorrectDaoMethod() = runBlocking {
        val notesToInsert = listOf(
            NoteEntry(1, "Note1", "Note1Text", 1612016883573, null, false, 1),
            NoteEntry(2, "Note2", "Note2Text", 1611998945723, null, false, 1),
            NoteEntry(3, "Note3", "Note3Text", 1611928273539, null, false, 1)
        )

        val notesInsertedCaptor = slot<List<DatabaseNote>>()
        noteRepository.insertNotes(notesToInsert)

        coVerify {
            noteDao.insertNotesList(capture(notesInsertedCaptor))
        }
        assertEquals(notesToInsert.toDatabaseList(), notesInsertedCaptor.captured)
    }

    @Test
    fun insertNote_ShouldInvokeCorrectDaoMethod() = runBlocking {
        val noteToInsert = NoteEntry(
            1,
            "Note1",
            "Note1Text",
            1612016883573,
            null,
            false,
            1
        )
        val noteCaptor = slot<DatabaseNote>()
        noteRepository.insertNote(noteToInsert)

        coVerify {
            noteDao.insert(capture(noteCaptor))
        }
        assertEquals(toDatabaseEntry(noteToInsert), noteCaptor.captured)
    }

    @Test
    fun updateNote_ShouldInvokeCorrectDaoMethod() = runBlocking {
        val noteToUpdate = NoteEntry(
            1,
            "Note1",
            "Note1Text",
            1612016883573,
            null,
            false,
            1
        )

        val noteCaptor = slot<DatabaseNote>()
        noteRepository.updateNote(noteToUpdate)

        coVerify {
            noteDao.update(capture(noteCaptor))
        }
        assertEquals(toDatabaseEntry(noteToUpdate), noteCaptor.captured)
    }

    @Test
    fun deleteNote_ShouldInvokeCorrectMethod() = runBlocking {
        val noteId = 1
        val noteIdCaptor = slot<Int>()

        noteRepository.deleteNote(noteId)

        coVerify {
            noteDao.deleteNote(capture(noteIdCaptor))
        }
        assertEquals(noteId, noteIdCaptor.captured)
    }
}