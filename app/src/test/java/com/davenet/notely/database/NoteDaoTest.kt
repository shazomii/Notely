package com.davenet.notely.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.davenet.notely.testutils.livedata.getValue
import com.davenet.notely.testutils.robolectric.TestRobolectric
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NoteDaoTest : TestRobolectric() {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: NotesDatabase
    private lateinit var noteDao: NoteDao
    private val fakeDatabaseNotes = listOf(
        DatabaseNote(1, "Note1", "Note1Text", 1612016883573, null, false, 1),
        DatabaseNote(2, "Note2", "Note2Text", 1611998945723, null, false, 1),
        DatabaseNote(3, "Note3", "Note3Text", 1611928273539, null, false, 1)
    )

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room
            .inMemoryDatabaseBuilder(context, NotesDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        noteDao = database.noteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun obtainAllNotesLiveData_WithoutData_ShouldReturnNull() {
        val notes = noteDao.getAllNotes()

        assertTrue(getValue(notes).isNullOrEmpty())
    }

    @Test
    fun obtainAllNotesLiveData_WithData_ShouldReturnSorted() = runBlocking {
        noteDao.insertNotesList(fakeDatabaseNotes)
        val notes = noteDao.getAllNotes()

        assertEquals(fakeDatabaseNotes, getValue(notes))
    }

    @Test
    fun obtainNoteById_WithoutData_ShouldNotBeFound() = runBlocking {
        val noteToFind = fakeDatabaseNotes.first()

        assertNull(noteDao.get(noteToFind.id!!))
    }

    @Test
    fun obtainNoteById_WithData_ShouldBeFound() = runBlocking {
        noteDao.insertNotesList(fakeDatabaseNotes)
        val noteToFind = fakeDatabaseNotes.first()

        assertEquals(noteToFind, noteDao.get(noteToFind.id!!))
    }

    @Test
    fun insertNote_ShouldAdd() = runBlocking {
        fakeDatabaseNotes.forEach {
            noteDao.insert(it)
        }

        assertEquals(fakeDatabaseNotes, getValue(noteDao.getAllNotes()))
    }

    @Test
    fun obtainLatestInsertedNote_ShouldBeFound() = runBlocking {
        noteDao.insertNotesList(fakeDatabaseNotes)
        val noteToFind = fakeDatabaseNotes.first()

        assertEquals(noteToFind, noteDao.getNote())
    }

    @Test
    fun deleteNote_Stored_ShouldDeleteIt() = runBlocking {
        noteDao.insertNotesList(fakeDatabaseNotes)
        val noteToDelete = fakeDatabaseNotes.first()
        noteDao.deleteNote(noteToDelete.id!!)

        assertThat(getValue(noteDao.getAllNotes()), not(hasItem(noteToDelete)))
    }

    @Test
    fun deleteNote_NotStored_ShouldDeleteNothing() = runBlocking {
        noteDao.insertNotesList(fakeDatabaseNotes)
        noteDao.deleteNote(4)

        assertEquals(fakeDatabaseNotes, getValue(noteDao.getAllNotes()))
    }

    @Test
    fun deleteSomeNotes_Stored_ShouldDeleteThem() = runBlocking {
        noteDao.insertNotesList(fakeDatabaseNotes)
        val notesToDelete = listOf(2, 3)
        noteDao.deleteSomeNotes(notesToDelete)

        assertThat(getValue(noteDao.getAllNotes()), hasItem(fakeDatabaseNotes.first()))
        assertThat(getValue(noteDao.getAllNotes()), not(hasItem(fakeDatabaseNotes[1])))
        assertThat(getValue(noteDao.getAllNotes()), not(hasItem(fakeDatabaseNotes[2])))
    }

    @Test
    fun updateNote_ShouldUpdateIt() = runBlocking {
        noteDao.insertNotesList(fakeDatabaseNotes)
        val updatedNote = DatabaseNote(1, "Note2", "Note2Text", 1622016883573, null, false, 1)
        noteDao.update(updatedNote)

        assertThat(getValue(noteDao.getAllNotes()), hasItem(updatedNote))
        assertEquals(updatedNote, noteDao.getNote())
        assertNotEquals(fakeDatabaseNotes.first(), noteDao.getNote())
    }
}