package com.davenet.notely.domain

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteEntryTest {

    @Test
    fun createNoteEntry_ShouldAddCorrectAttributes() {
        val noteId = null
        val noteTitle = "Note1"
        val noteText = "Note1Text"
        val createdDate = 1612016883573
        val noteReminder = null
        val reminderStarted = false
        val noteColor = 1

        val noteEntry = NoteEntry(
            id = noteId,
            title = noteTitle,
            text = noteText,
            date = createdDate,
            reminder = noteReminder,
            started = reminderStarted,
            color = noteColor
        )

        assertEquals(noteId, noteEntry.id)
        assertEquals(noteTitle, noteEntry.title)
        assertEquals(noteText, noteEntry.text)
        assertEquals(createdDate, noteEntry.date)
        assertEquals(noteReminder, noteEntry.reminder)
        assertEquals(reminderStarted, noteEntry.started)
        assertEquals(noteColor, noteEntry.color)
        Assert.assertNotEquals(noteColor, 2)
    }
}