package com.davenet.notely.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Database entities go in this file. These are responsible for reading and writing from the
 * database.
 */

/**
 * DatabaseNote represents a note entity in the database.
 */
@Parcelize
@Entity(tableName = "database_note")
data class DatabaseNote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String,
    var text: String
) : Parcelable {
    fun copy(): DatabaseNote {
        return DatabaseNote(id, title, text)
    }
}