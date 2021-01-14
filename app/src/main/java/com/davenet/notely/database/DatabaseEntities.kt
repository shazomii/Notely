package com.davenet.notely.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.davenet.notely.domain.NoteEntry

/**
 * Database entities go in this file. These are responsible for reading and writing from the
 * database.
 */

/**
 * DatabaseNote represents a note entity in the database.
 */
@Entity(tableName = "database_note")
data class DatabaseNote(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val title: String,
    val text: String,
    val date: Long?,
    val reminder: Long?,
    val started: Boolean,
    val color: Int
) {
    companion object {
        fun toDatabaseEntry(note: NoteEntry): DatabaseNote {
            return DatabaseNote(note.id, note.title, note.text, note.date, note.reminder, note.started, note.color)
        }
    }

    fun asDomainModelEntry(): NoteEntry {
        return NoteEntry(id, title, text, date, reminder, started, color)
    }
}

/**
 * Map DatabaseNotes to domain entities
 */
fun List<DatabaseNote>.asDomainModel(): List<NoteEntry> {
    return map {
        NoteEntry(it.id, it.title, it.text, it.date, it.reminder, it.started, it.color)
    }
}

fun List<NoteEntry>.toDatabaseList(): List<DatabaseNote> {
    return map {
        DatabaseNote(it.id, it.title, it.text, it.date, it.reminder, it.started, it.color)
    }
}

