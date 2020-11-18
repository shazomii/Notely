package com.davenet.notely.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * NoteDao Interface with helper methods
 * getNote() method fetches a note from the database
 * insert() method inserts a note into the database
 */
@Dao
interface NoteDao {
    @Query("select * from database_note order by date desc")
    fun getAllNotes(): LiveData<List<DatabaseNote>>

//    @Query("select * from database_note ORDER BY id DESC LIMIT 1")
//    fun getNote(): DatabaseNote

    @Query("delete from database_note where id = :key")
    fun deleteNote(key: Int?)

    @Query("delete from database_note")
    fun deleteAllNotes()

    @Insert
    fun insert(note: DatabaseNote?)

    @Insert
    fun insertNotesList(notes: List<DatabaseNote>)

    @Update
    fun update(note: DatabaseNote)

    @Query("select * from database_note where id = :key")
    fun get(key: Int): LiveData<DatabaseNote>
}