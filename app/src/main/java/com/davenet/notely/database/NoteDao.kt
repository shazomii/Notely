package com.davenet.notely.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * NoteDao Interface with helper methods
 *
 */
@Dao
interface NoteDao {
    /**
     * Retrieve a list of all notes in the database as LiveData
     *
     * @return [LiveData]<[List]<[DatabaseNote]>>
     */
    @Query("select * from database_note order by date desc")
    fun getAllNotes(): LiveData<List<DatabaseNote>>

    /**
     * Retrieve the latest inserted note from the database
     *
     * @return [DatabaseNote]
     */
    @Query("select * from database_note ORDER BY date DESC LIMIT 1")
    fun getNote(): DatabaseNote

    /**
     * Delete the Note with the given id from the database
     *
     * @param noteId [Int]
     */
    @Query("delete from database_note where id = :noteId")
    fun deleteNote(noteId: Int)

    /**
     * Delete the notes with the included ids from the database
     *
     * @param idList [List]<[Int]>
     */
    @Query("delete from database_note where id in (:idList)")
    fun deleteSomeNotes(idList: List<Int>)

    /**
     * Insert a single Note into the database
     *
     * @param note [DatabaseNote]
     */
    @Insert
    fun insert(note: DatabaseNote?)

    /**
     * Insert a list of Notes into the database
     *
     * @param notes [List]<[DatabaseNote]>
     */
    @Insert
    fun insertNotesList(notes: List<DatabaseNote>)

    /**
     * Update contents of a Note in the database
     *
     * @param note [DatabaseNote]
     */
    @Update
    fun update(note: DatabaseNote)

    /**
     * Retrieve a single note with the specified id from the database
     *
     * @param noteId [Int]
     * @return [DatabaseNote]
     */
    @Query("select * from database_note where id = :noteId")
    fun get(noteId: Int): DatabaseNote
}