package com.davenet.notely.database

import com.davenet.notely.database.NotesDatabase.Companion.getDatabase
import com.davenet.notely.testutils.robolectric.TestRobolectric
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Test

class NotesDatabaseTest : TestRobolectric() {

    @Test
    fun obtainNoteDao() {
        val notesDatabase: NotesDatabase = mockk()
        val noteDao: NoteDao = mockk()
        every { notesDatabase.noteDao() } returns noteDao

        assertThat(
            notesDatabase.noteDao(),
            instanceOf(NoteDao::class.java)
        )
    }

    @Test
    fun obtainNotesDatabase() {
        val notesDatabase = getDatabase(context)

        assertNotNull(notesDatabase.noteDao())
    }
}