package com.davenet.notely.database

import com.davenet.notely.database.DatabaseNote.Companion.toDatabaseEntry
import com.davenet.notely.domain.NoteEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseEntitiesTest {
    private val fakeDatabaseNotes = listOf(
        DatabaseNote(1, "Note1", "Note1Text", 1612016883573, null, false, 1),
        DatabaseNote(2, "Note2", "Note2Text", 1611998945723, null, false, 1),
        DatabaseNote(3, "Note3", "Note3Text", 1611928273539, null, false, 1)
    )

    private val fakeDomainNotes = listOf(
        NoteEntry(1, "Note1", "Note1Text", 1612016883573, null, false, 1),
        NoteEntry(2, "Note2", "Note2Text", 1611998945723, null, false, 1),
        NoteEntry(3, "Note3", "Note3Text", 1611928273539, null, false, 1)
    )

    @Test
    fun mapDatabaseEntitiesToDomainEntities_ShouldMap() {
        val domainNotesList = fakeDatabaseNotes.asDomainModel()

        assertEquals(domainNotesList, fakeDomainNotes)
    }

    @Test
    fun mapDatabaseEntityToDomainEntity_ShouldMap() {
        val domainNote = fakeDatabaseNotes.first().asDomainModelEntry()

        assertEquals(domainNote, fakeDomainNotes.first())
    }

    @Test
    fun mapDomainEntitiesToDatabaseEntities_ShouldMap() {
        val databaseNotes = fakeDomainNotes.toDatabaseList()

        assertEquals(databaseNotes, fakeDatabaseNotes)
    }

    @Test
    fun mapDomainEntityToDatabaseEntity_ShouldMap() {
        val databaseNote = toDatabaseEntry(fakeDomainNotes.first())

        assertEquals(databaseNote, fakeDatabaseNotes.first())
    }
}