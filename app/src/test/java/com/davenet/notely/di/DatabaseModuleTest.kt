package com.davenet.notely.di

import com.davenet.notely.database.NoteDao
import com.davenet.notely.database.NotesDatabase
import com.davenet.notely.testutils.robolectric.TestRobolectric
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class DatabaseModuleTest : TestRobolectric() {
    private lateinit var databaseModule: DatabaseModule

    @Before
    fun setUp() {
        databaseModule = DatabaseModule()
    }

    @Test
    fun verifyProvidedAppDatabase() {
        val notesDatabase = databaseModule.provideAppDatabase(context)

        assertNotNull(notesDatabase.noteDao())
    }

    @Test
    fun verifyProvidedNoteDao() {
        val notesDatabase: NotesDatabase = mockk()
        val noteDao: NoteDao = mockk()

        every { notesDatabase.noteDao() } returns noteDao

        assertEquals(
            noteDao,
            databaseModule.provideNoteDao(notesDatabase)
        )

        verify { notesDatabase.noteDao() }
    }
}