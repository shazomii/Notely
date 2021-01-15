package com.davenet.notely.domain

import com.davenet.notely.util.colors

/**
 * NoteEntry class represent the domain model i.e the
 * object visible to the app user.
 *
 * @param id        id of the note
 * @param title     title of the note
 * @param text      content of the note
 * @param date      date the note was created/modified
 * @param reminder  date set for a reminder in the note
 * @param started   whether or note the reminder is active
 * @param color     color of the note
 */
data class NoteEntry(
        var id: Int? = null,
        var title: String = "",
        var text: String = "",
        var date: Long? = null,
        var reminder: Long? = null,
        var started: Boolean = false,
        var color: Int = colors.random()
)