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
    var text: String
) : Parcelable {
    fun copy(): DatabaseNote {
        return DatabaseNote(id, title, text)
    }
}

fun List<NoteEntry>.asDataBaseModel(): List<DatabaseNote> {
    return map {
        DatabaseNote(
            id = it.id,
            title = it.title,
            text = it.text
        )
    }
}