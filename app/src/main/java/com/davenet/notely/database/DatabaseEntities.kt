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
    val id: Int? = 0,
    val title: String,
    val text: String,
    val date: Long?
)

/**
 * Map DatabaseNotes to domain entities
 */
fun List<DatabaseNote>.asDomainModel(): List<NoteEntry> {
    return map {
        NoteEntry(
            id = it.id,
            title = it.title,
            text = it.text,
            date = it.date
        )
    }
}