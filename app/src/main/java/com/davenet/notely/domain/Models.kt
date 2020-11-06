package com.davenet.notely.domain

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.davenet.notely.database.DatabaseNote
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class NoteEntry(
    @PrimaryKey
    var id: Int?,
    var title: String,
    var text: String,
    var date: Long?
) : Parcelable {
    fun copy(): DatabaseNote {
        return DatabaseNote(id, title, text, date)
    }
}

fun List<NoteEntry>.asDataBaseModel(): List<DatabaseNote> {
    return map {
        DatabaseNote(
            id = it.id,
            title = it.title,
            text = it.text,
            date = it.date
        )
    }
}